
<?php 
    error_reporting(E_ALL); 
    ini_set('display_errors',1); 

    include('dbcon.php');

    if( ($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit']))
    {
        $user_name=$_POST['user_name'];

        if(empty($user_name)){
            $errMSG = "errMSG errMSG errMSG";
        }

        if(!isset($errMSG))
        {
            try{
                $stmt = $con->prepare('INSERT INTO user_info (user_id, user_name) VALUES(null, :user_name)');
                $stmt->bindParam(':user_name', $user_name);

                if($stmt->execute())
                {
                    $successMSG = "새로운 사용자를 추가했습니다.";
                }
                else
                {
                    $errMSG = "사용자 추가 에러";
                }

            } catch(PDOException $e) {
                die("Database error: " . $e->getMessage()); 
            }
        }

    }
?>

<html>
   <body>
        <?php 
        if (isset($errMSG)) echo $errMSG;
        if (isset($successMSG)) echo $successMSG;
        ?>
        yguhuh
        <form action="<?php $_PHP_SELF ?>" method="POST">
            user_name: <input type = "text" name = "user_name" />
            <input type = "submit" name = "submit" />
        </form>
   
   </body>
</html>