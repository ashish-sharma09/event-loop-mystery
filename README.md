If I run main class and run ab:

```sh
ab -n 10000 -c 10 http://localhost:8086/nonBlocking
```

After few warum up runs I eventually get:

```sh
This is ApacheBench, Version 2.3 <$Revision: 1843412 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking localhost (be patient)
Completed 1000 requests
Completed 2000 requests
Completed 3000 requests
Completed 4000 requests
Completed 5000 requests
Completed 6000 requests
Completed 7000 requests
Completed 8000 requests
Completed 9000 requests
Completed 10000 requests
Finished 10000 requests


Server Software:        
Server Hostname:        localhost
Server Port:            8086

Document Path:          /nonBlocking
Document Length:        22 bytes

Concurrency Level:      10
Time taken for tests:   0.769 seconds
Complete requests:      10000
Failed requests:        2496
   (Connect: 0, Receive: 0, Length: 2496, Exceptions: 0)
Total transferred:      1487504 bytes
HTML transferred:       217504 bytes
Requests per second:    13011.41 [#/sec] (mean)
Time per request:       0.769 [ms] (mean)
Time per request:       0.077 [ms] (mean, across all concurrent requests)
Transfer rate:          1890.09 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0   0.1      0       2
Processing:     0    0   0.2      0       2
Waiting:        0    0   0.1      0       2
Total:          0    1   0.2      1       3

Percentage of the requests served within a certain time (ms)
  50%      1
  66%      1
  75%      1
  80%      1
  90%      1
  95%      1
  98%      1
  99%      2
 100%      3 (longest request)
```

but every second run I get:

```
This is ApacheBench, Version 2.3 <$Revision: 1843412 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking localhost (be patient)
Completed 1000 requests
Completed 2000 requests
Completed 3000 requests
Completed 4000 requests
Completed 5000 requests
Completed 6000 requests
apr_socket_recv: Operation timed out (60)
Total of 6373 requests completed
```

On my machine that happens exactly every second time, sometimes it does finish but the max is over 10s.