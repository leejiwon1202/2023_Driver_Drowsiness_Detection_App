<?php 
    error_reporting(E_ALL); 
    ini_set('display_errors',1); 

    include('dbcon.php');
    $android = strpos($_SERVER['HTTP_USER_AGENT'],"Android");

    if($android)
    {
        $user_id=$_POST['user_id'];
        $stmt = $con->prepare('select * from drowsy_data where user_id=:user_id');

        try{
            $stmt->bindParam(':user_id', $user_id);
            $stmt->execute();
        } catch(PDOException $e) {}


        if ($stmt->rowCount() > 0)
        {
            $data = array(); 

            while($row=$stmt->fetch(PDO::FETCH_ASSOC))
            {
                extract($row);
                array_push($data,  array('driving_id'=>$driving_id, 'user_id'=>$user_id, 'drowsy_time'=>$drowsy_time, 'elapsed_time'=>$elapsed_time));
            }

            header('Content-Type: application/json; charset=utf8');
            $json = json_encode(array("webnautes"=>$data), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
            echo $json;
        }
    }
?>