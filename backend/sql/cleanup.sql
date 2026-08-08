DELETE FROM device_metric WHERE ts < NOW() - INTERVAL 1 DAY;
DELETE FROM alert WHERE ack = 1 AND created_at < NOW() - INTERVAL 30 DAY;
DELETE FROM access_log WHERE created_at < NOW() - INTERVAL 7 DAY;
