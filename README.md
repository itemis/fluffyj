# Fluffy J
A library that tries to put a little fluff ❤ into the daily life of Java developers.
  
It does sacrifice code precision and expressiveness for readability, meaning it can't do everything and is not highly customizable by purpose. It is not and shall never be as powerful and big as the popular libraries like [Guava](https://github.com/google/guava), it just happens to be a collection of things that match my taste and proved to be very helpful during my recent years as a professional developer. Nothing fancy, no wheel reinvented, just some fluff.  

## Build  
Note: JDK >=17 is required for the build.  

`mvn clean install`

# Sneaky
With `com.itemis.fluffyj.sneaky.Sneaky` it is possible to throw checked exceptions without the need to declare them. This is done by using the so called **Sneaky Throws Paradigm**, hence the name.
  
This may come in handy in some edge cases where throwing a Throwable is not permitted by the compiler, but wrapping it in a `RuntimeException` may cause pain when handling errors on higher levels of the call stack. Also it may just be very uncomfortable or impossible to do proper error handling within a lambda expression. This is especially true for legacy code that does not allow checked exceptions to be thrown but does also not permit breaking changes to public interfaces. With Sneaky, you may just do it like this:
  
```
  // LegacyApi expects a Supplier that does not specify 'throws IOException'
  legacyApi.process(() -> {
      try {
          someOp();
      } catch (Exception e) {
          Sneaky.throwThat(e);
      }
  });
```
  
This kind of thing is usually a smell and only valid in a few situations. Please be careful when doing so as clients are usually not aware of checked exceptions that come out of methods that do not declare them.

# Exceptions
The package `com.itemis.fluffyj.exceptions` contains code that deals with Throwables.

## ImplementationProblemException
`ImplementationProblemException` may be used to indicate a mistake in the implementation, e. g. a missing default branch in a switch statement. Developers tend to throw a RuntimeException in such a case. `ImplementationProblemException` aims at providing a consistent more precise default way to handle these kind of things, e. g.  

```
switch (type) {
    case cat: ...
    case dog: ...
    default: throw new ImplementationProblemException("Unknown type");
}
```

## InstantiationNotPermittedException  
`InstantiationNotPermittedException` aims at providing a default way to indicate that a constructor may not be used. This may be used to make sure, a class is never instantiated. This is usually helpful to make sure static helpers are not instantiated by some "smart" reflective code, e. g.:  
  
```
// This class contains static helper code only and cannot be instantiated, even not by reflection.
public final class Clazz() {
    private Clazz() {
        throw new InstantionNotPermittedException();
    }
}
```
  
## ThrowablePrettyfier
When handling Throwables (usually Exceptions) they often must be logged. When doing so, I often encountered the following pains:
* Don't want to log the whole stack trace, so `log.error("error", e)` is inconvenient.
* Want to log the message but it may be `null` and we don't want to log `null`, nor do we want to do `if (e.getMessage == null)` all the time.
* Want to log the exception's type to make things more clear but don't want to call e.getClass().getSimpleName() all the time.
  
So the goal here is to reduce redundant boiler plate code in catch blocks.
  
In those cases you may just do `ThrowablePrettyfier.pretty(e)`, e. g.:

```
catch (NullPointerException e) { // has null message
    log.error(ThrowablePrettyfier.pretty(e)) // Log message is: NullPointerException: No further information.
}
```
  
Since the `ImplementationProblemException` uses the prettyfier, you could even do something like this:

```
catch (NullPointerException e) {
    // Results in: An implementation problem occurred: NullpointerException: No further information.
    log.error(new ImplementationProblemException(e));
}
```

# Concurrency
The package `com.itemis.fluffyj.concurrency` contains code that makes working with concurrent / asynchronous Java code more streamlined.  
  

I always though that Java's concurrency API (`java.util.concurrent`) is as powerful as it is misleading at times. E. g. you cannot just `kill` an Executor, giving threads a meaningful name is a bit bulky and waiting on Futures or Latches needs handling of up to 4 different types of Exceptions. 
  
This is ok in situations where you need this kind of precision, but if just want to run some operation in multiple threads and not clutter code with concurrency details, you may do it like this:

```
// generates short ids that do not clutter your log like UUIDs would.
var threadNameFactory = new UniqueShortIdThreadNameFactory();

var handle = new ExecutorServiceHandle(THREAD_COUNT, threadNameFactory);
var future = handle.getExecutor().submit(() -> someLongRunningOperation());

// Do something else

//Eventually
SomeClass result = null;
try {
    result = FluffyFutures.waitOnFuture(future, Duration.ofSeconds(5));
} catch (RuntimeException e) {
    log.error("Waiting on op to complete caused a problem: " + ThrowablePrettyfier.pretty(e));
} finally {
    handle.kill(Duration.ofSeconds(5));
}

return result;
```
  
Note how the example uses `FluffyFutures` to wait on a future to complete. A similar thing is possible with `FluffyLatches` (synchronizing on latches) or `FluffyExecutors` (straight forward killing of Executors).