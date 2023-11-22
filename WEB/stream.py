import requests
import cv2 
import numpy as np
import time
import math
from resnet3D import resnet3D50
import torch.nn as nn
import torch
from flask import Flask,render_template,Response
from flask_socketio import SocketIO, emit
import mediapipe as mp


# 1)
STATIC_FOLDER = 'templates/assets'
app = Flask(__name__, static_folder=STATIC_FOLDER)
async_mode = None
socketio = SocketIO(app, async_mode=async_mode)


# 2)
def getUserState(model, input):
  input = torch.Tensor((input)).unsqueeze(0)
  result = model(input)[0].detach().cpu().numpy()
  print(result)

  if result[1] >= 1:
    return ("Drowsy!!!", (0, 0, 255)) # drowsy 판별
  else:
    return ("Normal", (255, 0, 0))


# 3)
def calc_ear(p_list):
  a_x = (p_list[0].x - p_list[3].x) ** 2 * 1000
  a_y = (p_list[0].y - p_list[3].y) ** 2 * 1000
  a = a_x + a_y
            
  b_x = (p_list[1].x - p_list[5].x) ** 2 * 1000
  b_y = (p_list[1].y - p_list[5].y) ** 2 * 1000
  b = b_x + b_y
            
  c_x = (p_list[2].x - p_list[4].x) ** 2 * 1000
  c_y = (p_list[2].y - p_list[4].y) ** 2 * 1000
  c = c_x + c_y
    
  return (b + c) / (2 * a)


# 4)
def generate_frames():
  camera=cv2.VideoCapture(0)
  
  model_path = './work_model_29.pth'
  model = resnet3D50(non_local=True)
  model.fc = nn.Linear(2048,2)
  model.load_state_dict(torch.load(model_path, map_location=torch.device('cpu')))
  data = np.ones(shape=(3, 150, 320, 240), dtype=np.int8)

  mp_face_mesh = mp.solutions.face_mesh

  cnt_frame = 0
  text_color = (255, 0, 0)
  user_state = "Wait.."
  
  eye_state = "Opened"
  list_index = [359, 387, 385, 362, 380, 373, 130, 160, 158, 133, 153, 144]
  r_mar = 0
  l_mar = 0
  area_Saved = False
  time_close = time.time()

  with mp_face_mesh.FaceMesh(max_num_faces=1, refine_landmarks=True, min_detection_confidence=0.5, min_tracking_confidence=0.5) as face_mesh:
    while True:
      success,frame = camera.read()
      if not success: break

      results = face_mesh.process(frame)
      if results.multi_face_landmarks:
        # 1) EAR
        results = face_mesh.process(frame)
        if results.multi_face_landmarks:
          list_coord = []
          for i in list_index:
            list_coord.append(results.multi_face_landmarks[0].landmark[i])
          l_ear = calc_ear(list_coord[:6])
          r_ear = calc_ear(list_coord[6:])

          if cnt_frame < 100 and not area_Saved:
            r_mar += r_ear; l_mar += l_ear
          elif not area_Saved:
            area_Saved = True
            r_mar /= 100;   l_mar /= 100
          elif (r_ear < r_mar * 0.8) and (l_ear < l_mar * 0.8):
            if eye_state == "Opened":
              time_close = time.time()
            if time.time() - time_close >= 3:
              socketio.emit('response', {'text': "warning"})
              socketio.sleep(1)
            eye_state = "Closed"
          else:
            eye_state = "Opened"

        # 2) 모델 활용
        roi = frame[:, 140:500].copy()
        img = cv2.resize(roi, (240, 320))

        data[:, :-1, :, :] = data[:, 1:, :, :]
        data[0, -1, :, :] = img[:,:,0]
        data[1, -1, :, :] = img[:,:,1]
        data[2, -1, :, :] = img[:,:,2]

        cnt_frame += 1
        if cnt_frame == 150:
            user_state, text_color = getUserState(model, data)
            cnt_frame = 0
      else:  # 화면 상에 사람이 없는 상태
        area_Saved = False
        r_mar = 0
        l_mar = 0
        user_state = "Wait.."

      # 3) WEB에 전송하기 위한 frame 설정
      frame = cv2.flip(frame, 1)
      cv2.putText(frame, user_state, (30, 50), cv2.FONT_ITALIC, 1, text_color, 2)
      _, buffer=cv2.imencode('.jpg',frame)
      frame=buffer.tobytes()
      yield(b'--frame\r\n' b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')


@app.route('/')
def index():
    return render_template('index.html')

@app.route('/video')
def video():
    return Response(generate_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')

if __name__=="__main__":
    app.run(debug=True)