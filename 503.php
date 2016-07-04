<?php
$uri = $_SERVER['REQUEST_URI'];
if (strpos($uri, '503.php') !== false) {
	header("Location:/");
} else {
	exec('sudo /srv/legat.ml/scripts/restart_play.sh > /dev/null 2> /dev/null &');
	while(!(bool)fsockopen('127.0.0.1', 9000, $errno, $errstr, 2)) {
		sleep(1);
	}
	sleep(2);
	header("Refresh:0");
}
