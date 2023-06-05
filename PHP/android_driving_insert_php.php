<?php 
    error_reporting(E_ALL); 
    ini_set('display_errors',1); 

    include('dbcon.php');
    $android = strpos($_SERVER['HTTP_USER_AGENT'],"Android");

    if($android)
    {
        $user_id=$_POST['user_id'];
        $s_time=$_POST['s_time'];
        $e_time=$_POST['e_time'];

        try{
            $stmt = $con->prepare('INSERT INTO driving_data (driving_id, user_id, s_time, e_time) VALUES(null, :user_id, :s_time, :e_time)');
            $stmt->bindParam(':user_id', $user_id);
            $stmt->bindParam(':s_time', $s_time);
            $stmt->bindParam(':e_time', $e_time);
            $stmt->execute();
        } catch(PDOException $e) {}
    }
?>