SELECT * FROM drowsy_schema.drowsy_data WHERE user_id=22;

UPDATE drowsy_schema.drowsy_data SET elapsed_time = 63 WHERE drowsy_time='2023-06-05 22:42:31' and driving_id=19 and user_id=22;
UPDATE drowsy_schema.drowsy_data SET elapsed_time = 64 WHERE drowsy_time='2023-06-05 22:42:37' and driving_id=19 and user_id=22;
UPDATE drowsy_schema.drowsy_data SET elapsed_time = 99 WHERE drowsy_time='2023-06-05 22:42:44' and driving_id=19 and user_id=22;

UPDATE drowsy_schema.drowsy_data SET elapsed_time = 123 WHERE drowsy_time='2023-06-06 15:32:03' and driving_id=22 and user_id=22;
