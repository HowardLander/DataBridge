#!/usr/bin/env python
import pika
import sys, getopt

def main(argv):
   server = ''
   queueName = ''

   if len(sys.argv) != 5:
      print 'log.py -s <server> -q <queue>'
      sys.exit(2)

   try:
      opts, args = getopt.getopt(argv,"hs::q:",["server=","queue="])
   except getopt.GetoptError:
      print 'log.py -s <server> -q <queue>'
      sys.exit(2)
   for opt, arg in opts:
      if opt == '-h':
         print 'log.py -s <server> -q <queue>'
         sys.exit()
      elif opt in ("-s", "--server"):
         server = arg
      elif opt in ("-q", "--queue"):
         queueName = arg
   connection = pika.BlockingConnection(pika.ConnectionParameters(host=server))
   channel = connection.channel()
   channel.queue_declare(queue=queueName)

   print ' Waiting for log messages. To exit press CTRL+C'

   def callback(ch, method, properties, body):
      print body

   channel.basic_consume(callback, queue=queueName, no_ack=True)
   channel.start_consuming()

if __name__ == "__main__":
   main(sys.argv[1:])
