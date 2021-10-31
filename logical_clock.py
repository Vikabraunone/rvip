from multiprocessing import Process, Pipe
from os import getpid
from datetime import datetime

#lj - логическое время


def local_time(lj):
    return '(LAMPORT_TIME={}, LOCAL_TIME={})'.format(lj, datetime.now())


def calc_receive_timestamp(lmsg, lj):
    return max(lj, lmsg) + 1


def event(pid, lj):
    lj += 1  # перед выполнением события увеличиваем показания часов
    print(f'Событие произошло в {pid} {local_time(lj)}')
    return lj


def send_message(pipe, pid, lj):
    lj += 1  # в момент отправки увеличиваем показания часов
    pipe.send(('Message', lj))
    print(f'Сообщение отправлено от {pid} {local_time(lj)}')
    return lj


def receive_message(pipe, pid, lj):
    msg, lmsg = pipe.recv()
    lj = calc_receive_timestamp(lmsg, lj)
    print(f'Сообщение получено от {pid} {local_time(lj)}')
    return lj


def process_one(pipe12):
    pid = getpid()
    lj = 0
    lj = event(pid, lj)
    lj = send_message(pipe12, pid, lj)
    lj = event(pid, lj)
    lj = receive_message(pipe12, pid, lj)
    lj = event(pid, lj)


def process_two(pipe21, pipe23):
    pid = getpid()
    lj = 0
    lj = receive_message(pipe21, pid, lj)
    lj = send_message(pipe21, pid, lj)
    lj = send_message(pipe23, pid, lj)
    lj = receive_message(pipe23, pid, lj)


def process_three(pipe32):
    pid = getpid()
    lj = 0
    lj = receive_message(pipe32, pid, lj)
    lj = send_message(pipe32, pid, lj)


if __name__ == '__main__':
    pipe12, pipe21 = Pipe()
    pipe23, pipe32 = Pipe()

    p1 = Process(target=process_one, args=(pipe12,))
    p2 = Process(target=process_two, args=(pipe21, pipe23))
    p3 = Process(target=process_three, args=(pipe32,))

    p1.start()
    p2.start()
    p3.start()

    p1.join()
    p2.join()
    p3.join()
