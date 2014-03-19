# Day 1

## Terminology

OOP - Ordinary Object Pointer ... pointer in the C/C++ sense

## command line tools

CLI tools have the benefit of being able to run locally. VisualVM etc use jmx which can be difficult to use in a firewalled setting.

### jmap

Use to snapshot memory usage; there aren't likely to be any domain objects in the top 10, so that may indicate a memory leak.

<> == things which are in PermGen

### jstack

Use to spot lock contention; don't snapshot more that once every 100ms or the snapshotting will affect the running application.

threads in BLOCKED which are locked on the same monitor may be deadlocked.

### javap

java code does not itself have a performance meaning; neither does bytecode. It's only when we compile down to machine code that we can understand performance. Especially with mutlithreaded code it's not worth focssing on the source code to reason about the behaviour.

### Jdeps

Available in JDK8

Static dependency analyser. What does the program depend on? Is the program dependent diretly on jdk internals - protrability problem and not able to run on non-oracle implementations.

## The JDK

### Packages of Note

jdk -- class libraries
hotspot -- the vm
langtools -- things to de with compilation

System.currentTimMiliis() -> (CStub) currentTimeMilis() -> (Hotspot) static C++ method


## Java IO

###  PLurals classes

* Arrays
* Collections
* Objects
* Files

Collection (interface)
 -> AbstractSomething (impl)
Collections (static methods)

The interface and the static methods are related to one another - it's a workaround for the absence of static methods on interfaces.

### Arrays

* Multidimensional arrays implementation

(heap) [] -> [a][b][c][d][e][f]
([a]) -> [a00][a01][a02]
([b]) -> [b00][b01][b02]

No guarentee that the arrays in a multidimensional array are alloc'd contiguously.

* Search

Arrays.binarySearch()

### Collections

Arrays.asList() returns Arrays$ArrayList() - this is an inner static class of Arrays.  It will throw UnsupportedOperationException for some calls whereas ArrayList will not.

Collecitons.shuffle() - can be used for testing with randomly ordered data.

### Objects (JDK7)

Objects.requiresNonNull()

### Files

There is no support for non-blocking fs operations.

The AutoClosable interface -- everything with a close method is automatically autoclosable. In a TWR block, only AutoClosable classes are allowed to be initialized. The reverse-order close behaviour is detailed in a paper by Jo Darcy.

* Path

We can manipulate non-file things, such as classpath elements; also things like zipfiles.

Files.copy() - default behaviour is not to overwrite on copy; this must be specified as a CopyOption.

## Using NIO

### Synchronous NIO

Java's char is not a complient implementation of UCS2 encoding due to width constraints.

ByteBuffer -> Channels

* writing bytes to a channel sends them out to a socket.
* For TCP, SocketChannel

### Async

Java 7 introduces runtime managed concurrent features, like async channels.

MultiCatch blocks - catch (IOException | FileNotFoundExceptione ) {}

Futures.isDone(), .get(); the thread pool is vm managed. It is possible to supply your own thread pool.

CompletionHandler is an alternative to Futures which uses callbacks.

## HTTP

GET shouldn't be used to change server state
* GETs can be resent
* GET responses can be cached by reverse proxys

HEAD is the most common request - used to implement client side caching, eg if modified header is changed.

## GC

Types of GC
* conservative - has no knowledge of the type
* exact - knows the type it is examining

Conservative can't tell whether it's examining an integer or a pointer.. Use exact if you want to be able to follow pointers.

### Approaches

* mark and sweep - allocation table lists objects; GC creates transitive closure over the AT and sets its mark bit. This is a binary partition; anything without its mark bit set is unreferenced and 'dead' so should be GCd.

Forming a transitive closure requires all processing to stop. Code is halted at a 'safe point'.

Weak generationnal hypothesis - the java heap has a specifically defined shape to take advantage of it. Implemented using evacuating collectors. A lot of what drives the jit compiler is based on collecting statistics to drive the runtime. The JVM modifies its management behaviour based on the program.

Evacuating moves live objects into new memory pools, the the entire space is wiped by GC.

Eden -> Suvivor Space 0 and 1 -> Old -> Perm

Survivor space is hemispherical - objects bounce between the two pools as they are alternately wiped, until the objects become 'old'. By default 'old' means having survibed 4 sweeps. This is visible in VisualGC.

### OutOfMemoryError

If <2% of the heap is cleared by full GC, throws OutOfMemoryError. >=98% time spent in GC (no progress). If the allocation is begger than Eden.

### GC Logging

Always run a GC log, especially in production.

## Weak and soft references

Weak/ soft references (java.lang.ref.Reference) may be available for collection while references exist. It might be used to create a caching implementation. 

* Soft references are collected before OOM errors
* Weak references are always collected at GC time

## Finalization (never use Finalization)

Java's finalization semantic is non-deterministic. TWR is a deterministic way of handling resources.

Finalizer has to be run on a separate thread. If something throws, there is no exception handling can be done because inter-thread communication is difficult (close on throw - but now the thread can't throw).

Finalize does not guarentee that resources will be cleaned up. The best way to clean up resources is to use TWR.

## Class objects and klassOops

klassOops are the representations of Java objects in the JVM. These live in perm gen. The Class object lives in the main part of the heap.

## Loose ends

See the source at hotspot/src/share/vm/oops

More info on perm gen changes (removal of perm gen) by searching under the subject 'meta space'. (Downside, there's a lack of good tooling for native memory analysis).

Class loading is a significant bottleneck, next to GC.

# Day 2

## Java concurrency

2 different concurrency mechanisms

* java.util.concurrent - not dependent on OS
* Concurrency - heavily dependent on OS features

## Values

Java has no equivalent to pointer arithmetic; mamory is not layed out in a detailed low level way.

* privative values - cannot be mutated
* reference values - the object can be altered

Java is pass by value, the value is a copy of the object reference (C++ &ref). This is pretty clear if you try to change the pointer as a side effect.

## Terminology

* live - concurrent task making progress towards a new state, cf deadlocked
* concurrent - two or more tasks
* parallel - live concurrent tasks

## Type safety

Code which prevents inconsistant state from being seen is 'concurrently type safe'.

* Shared, visible mutable state - 
* Preemptive thread scheduling - can be stopped at any time
* Locking

Threads have

* stack
* copies of registers including pc

threads share

* virtual address space

A thread has its own view of how execution is happening, but shares memory.

* context switch requires a flush of the memory tables.
* lightweight context switch only requires registers to be swapped out.

Threads can be suspended at any safe point.

### Synchronized

Modifications by code outsite a synchronized block will see the inconsistent state - synchronized is fragile.

Synchronized means memory is read at the start of the block, and write back occurrs at the end of the block.

Synchronized means all threads have the same view of memory.

Static synchronised methods lock the class object.

MuClass.class - locks all instances of base class and derrived classes
MyClass.getClass() - locks all instances of the specified class

Java locks are reentrant - if a lock is held, it is possible to pass a block which depends on the same lock.

### Volatile

Without vollatile there's no determinism about when a change to a value is visible to other tasks. Volatile flushes caches, causing a cache miss and a view of the updated value. ie. it is possible that an update in one thread doesn't propagate out of the cache for some amount of time.

Provides an edge with a happens before relationship.

* read from memory
* modify
* write to memory

Any modification to a volatile value is seen by all threads. There is no guarantee that the updates to the volatile value will not be inconsistant if they are not all synchronised.

### The builder pattern

* create copies
* return immutable objects

Using mutable objects as keys in a hash map is a bad idea as a key. Altering the mutable values can cause the hash code to perform differently, and may be lost inside the map.

## Threads

Naming threads with setName() - good idea!

stop, suspend resume countStackFrames destroy - are all dreadful, dreadful methods that will cause deadlocks and inconssitant state.

### Avoiding deadlocks

* acquire all the locks in the same order
* Concurrency has Lock strategies

## JMM

* 'happens before' defines a transitive edge between two tasks

CopyOnWriteArrayList

* we do an add - creates a happens before edge
* a get - we are guaranteed there is a happens before edge before this

## java.util.concurrent

### Lock

j.u.c doesn't use kernal locks

## Streams

There is (almost) no computation when methods are called on a stream. Computation is done when the pipeline is terminated, eg. by collect().

Stream's parallel friend is the parallelStream method, eg
Collection::stream -> Collection::parallelStream()

## Loose ends

Strings are mutable - the hash code is constructed on demand to avoid the need for a possibly unused linear scan of the string. This makes string state very difficult to reason about.

Effective Java recommendation

Sizing a threadpool - 2 n CPU as an initial value
