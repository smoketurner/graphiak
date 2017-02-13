#!/usr/bin/env python

import socket
import time

HOST = '127.0.0.1'
PORT = 2003
COUNT = 1000000

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((HOST, PORT))

success_count = 0

start_time = time.clock()

for x in xrange(COUNT):
    ret = sock.sendall('local.random.diceroll 4 {}\n'.format(int(time.time())))
    if ret is None:
        success_count += 1

sock.close()

interval = time.clock() - start_time

print 'Sent {} metrics in {} seconds or {} metrics/second'.format(
    COUNT, interval, int(COUNT / interval))
