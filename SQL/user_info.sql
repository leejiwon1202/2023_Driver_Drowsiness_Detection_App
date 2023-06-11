SELECT * FROM drowsy_schema.user_info ORDER BY user_id ASC; 

# ALTER TABLE drowsy_schema.user_info ADD UNIQUE (user_name);
DELETE FROM drowsy_schema.user_info WHERE user_id between 16 and 22
