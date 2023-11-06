from flask import Flask,render_template,Response, jsonify
import cv2 
import torch
import numpy as np
from resnet3D import resnet3D50
import torch.nn as nn

STATIC_FOLDER = 'templates/assets'
app = Flask(__name__,
            static_folder=STATIC_FOLDER)
camera=cv2.VideoCapture(0)
value = "Wait.."

def generate_frames():
    n = 0
    value = "Wait.."
    data = np.ones(shape=(3, 150, 320, 240), dtype=np.int8)

    device = 'cpu'

    model_path = './work_model_29.pth'
    model = resnet3D50(non_local=True).to(device)
    model.fc = nn.Linear(2048,2).to(device)
    model.load_state_dict(torch.load(model_path, map_location=torch.device('cpu')))

    while True:
        success,frame=camera.read()
        n += 1
        
        if not success:
            break
        else:
            roi = frame[:, 140:500].copy()
            img = cv2.resize(roi, (240, 320))

            data[:, :-1, :, :] = data[:, 1:, :, :]

            data[0, -1, :, :] = img[:,:,0]
            data[1, -1, :, :] = img[:,:,1]
            data[2, -1, :, :] = img[:,:,2]

            if n == 150:
                input = torch.Tensor(data).unsqueeze(0).to(device)
                result = model(input)[0].detach().cpu().numpy() 
                print(result)
                
                if result[1] >= 2: value = "Drowsy!!!"
                else: value = "Normal"

                n = 0

            frame = cv2.flip(frame, 1)
            cv2.putText(frame, value, (30, 50),
                        cv2.FONT_ITALIC, 1, (255, 0, 0), 2)

            ret,buffer=cv2.imencode('.jpg',frame)
            frame=buffer.tobytes()

        yield(b'--frame\r\n' b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')

@app.route('/')
def index():
    return render_template('index2.html', value=value)

@app.route('/video')
def video():
    return Response(generate_frames(),mimetype='multipart/x-mixed-replace; boundary=frame')

@app.route('/hello', methods=["POST"])
def hello():
    data = {'value': 'hello'}
    return jsonify(data)

if __name__=="__main__":
    app.run(debug=True)
