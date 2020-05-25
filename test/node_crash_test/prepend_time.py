from timeit import default_timer as timer

t0 = timer()
while True:
    try:
        s = input()
        t = timer()
        print("%8.3f %s" % (t-t0,s)) 
    except:
        break
