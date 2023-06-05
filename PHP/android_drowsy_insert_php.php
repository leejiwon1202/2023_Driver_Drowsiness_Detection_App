<?php 
    error_reporting(E_ALL); 
    ini_set('display_errors',1); 

    include('dbcon.php');
    $android = strpos($_SERVER['HTTP_USER_AGENT'],"Android");

    if($android)
    {
        $driving_id=$_POST['driving_id'];
        $user_id=$_POST['user_id'];
        $drowsy_time=$_POST['drowsy_time'];
        $elapsed_time=$_POST['elapsed_time'];

        try{
            $stmt = $con->prepare('INSERT INTO drowsy_data (driving_id, user_id, drowsy_time, elapsed_time) VALUES (:driving_id, :user_id, :drowsy_time, :elapsed_time)');
            $stmt->bindParam(':driving_id', $driving_id);
            $stmt->bindParam(':user_id', $user_id);
            $stmt->bindParam(':drowsy_time', $drowsy_time);
            $stmt->bindParam(':elapsed_time', $elapsed_time);
            $stmt->execute();
        } catch(PDOException $e) { }
    }
?>