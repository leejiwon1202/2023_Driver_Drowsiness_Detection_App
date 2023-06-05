<?php 
    error_reporting(E_ALL); 
    ini_set('display_errors',1); 

    include('dbcon.php');
    $android = strpos($_SERVER['HTTP_USER_AGENT'],"Android");

    if($android)
    {
        $user_name=$_POST['user_name'];

        try{
            $stmt = $con->prepare('INSERT INTO user_info (user_id, user_name) VALUES(null, :user_name)');
            $stmt->bindParam(':user_name', $user_name);
            $stmt->execute();
        } catch(PDOException $e) {}
    }
?>