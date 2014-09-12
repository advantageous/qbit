Boon : qbit - Go Channels inspired Java lib.
====

Everything is a queue. You have a choice. You can embrace it and control it. You can optimize for it. 
Or you can hide behind abstractions.

QBit opens you up to peeking into what is going on, and allows you to pull some levers without selling your soul.

QBit can be 15x faster than built-in Java Queues.


Status
=====
Early Days.



QBit and Boon philosiphy:
====
At the end of the day QBit is a simpley library not a framework. 
Your app is not a QBit app but a Java app that uses the QBit lib.
QBit allows you to work with Java UTIL concurrent, and does not endeavor to hide it from you.
Just trying to take the sting out of it. 



QBit a Boon project
=====

QBit is a Boon sub project.  Boon was (and still is) too big.
It is time to start breaking it up.
Boon is large enough now that I often forget features that I added until I see bug reports for them. 


Does it work
=====
I think so.
I have used techniques in Boon and QBit with great success in high-end, high-performance, high-scalable apps. 
I have helped clients handle 10x the load with 1/10th the servers of their competitors using techniques in QBit.
QBit is me being sick of hand tuning queue access and threads.
Also I have the same QBit like thing written about 20x times. 
And if I write it one more time, I am going to go insane.
I often twist and turn knobs for each use case, but often forget which knob to tweak in which sceanrio.


Single Writer, Mulit Write with CPU bound writer do this, this and this.
Single Writer, Multi Writer with IO bound writer do this, this and this.
and so on and so on...


Boon and QBit humility policy
=====
Ideas for Boon and QBit often come from all over the web. We make mistakes. Point them out. 
As a developer of Boon and QBit, I am a fellow traveler. 
If you have an idea or technique you want to share, I am all ears. 


Inspiration
====

A big inspireation for Boon/QBit was Go Channels, Active Objects, Apartment Model Threading, Actor, and Mechnical Sympathy papers.
I have read the AKKA in Action Book. It was inpsiring, but not the only inspiration for QBit and not the major inspriation.
QBit has ideas that are similar to many frameworks. 
We are all reading the same papers. 
Most of the inpiration for QBit was the LMAX disruptor papers and this blog http://php.sabscape.com/blog/?p=557.
I had a theory about queues that this blog post (http://php.sabscape.com/blog/?p=557) inprired me to try out. 
 
Does QBit compete with...
====
Spring Disruptor: No. You could use QBit to write plugins for Spring Disruptor I suppose, but QBit does not compete with Spring Disruptor.
Akka: No. Ditto
LMAX Disruptor: No.

Early Benchmarks
====

```

 Description                        QBIT(ms)          LinkedBlockingQueue(ms)                         %Better
 One Reader, One Writer                6276                            10,003                          159.38
 Two Reader, One Writer                4235                             9,105                          214.99
 Ten Readers, One Writer                586                             9,196                        1,542.95
 10 Readers, 10 Writers                4782                            15,182                          317.48
 1 Readers, 10 Writers               40,618                            16,472                         -246.59     QBIT LOST!
 2 Readers, 10 Writer                16,491                            18,342                          111.22
 5 Readers, 10 Writers               10,598                            17,587                          165.95
 10 Readers, 1 Writer                   316                             1,616                          511.39
 10 Readers, 5 Writer                 1,060                             7,589                          715.94
```

As you can see, QBIT does quite well. But there is one case where it does not do as well.
The solution is simple. There will be a factory where you can specify number of readers/writers, IO Bound/CPU Bound, etc.
Then QBIT will use the write queue based on the factory params. 
Currently QBIT uses LinkedTransferQueue at the moment. 
Anyway. Check back. Work in progress....
