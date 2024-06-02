package org.eurocris.openaire.cris.validator.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import junit.framework.AssertionFailedError;

/**
 * An {@link Iterable} collection that will check certain facts either upon returning every object or after all objects have been iterated.
 * Add more checks by wrapping.
 * @author jdvorak
 *
 * @param <T> the type of the elements of the collection
 */
public abstract class CheckingIterable<T> implements Iterable<T> {

	/**
	 * Iterate through the elements and call {@link #close()} at the end.
	 * @return the number of elements iterated
	 */
	public long run() {
		long n = 0;
		final Iterator<T> it = iterator();
		while ( it.hasNext() ) {
			it.next();
			++n;
		}
		close();
		return n;
	}

	/**
	 * Extend here to place the checks to do after all the elements of the collection have been visited.
	 */
	protected abstract void close();

	/**
	 * A simple CheckingIterable that in fact doesn't check anything yet.
	 * Wrap it to do the checks.
	 * @param list the elements to iterate over
	 * @return a CheckingIterable
	 */
	public static <T> CheckingIterable<T> over( final Iterable<T> list ) {
		return new CheckingIterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return list.iterator();
			}

			@Override
			protected void close() {
				// no-op
			}
			
		};
	}
	
	/**
	 * When iterating, call the given function on each element.
	 * The result of this call is sent further up the CheckingIterable chain.
	 * @param <U> the target class of the mapping
	 * @param f the function to apply
	 * @return a CheckingIterable
	 */
	public <U> CheckingIterable<U> map( final Function<T,U> f ) {
		final CheckingIterable<T> parentChecker = this;
		return new CheckingIterable<U>() {

			@Override
			public Iterator<U> iterator() {
				final Iterator<T> parentIterator = parentChecker.iterator();
				return new Iterator<U>() {

					@Override
					public boolean hasNext() {
						return parentIterator.hasNext();
					}

					@Override
					public U next() {
						final T x = parentIterator.next();
						return f.apply( x );
					}
					
				};
			}

			@Override
			protected void close() {
				parentChecker.close();
			}

		};		
	}

	/**
	 * Build a CheckingIterable which checks that the collection contains at least one element for which the given predicate is true.
	 * @param predicate the condition to look for
	 * @param message to signal when no matching element is found
	 * @return this CheckingIterable wrapped to check the condition
	 */
	public CheckingIterable<T> checkContains( final Predicate<T> predicate, final String message ) {
		return checkContains( predicate, new AssertionFailedError( message ) );
	}
	
	/**
	 * Build a CheckingIterable which checks that the collection contains at least one element for which the given predicate is true.
	 * @param predicate the condition to look for
	 * @param error to raise when no matching element is found
	 * @return this CheckingIterable wrapped to check the condition
	 */
	public CheckingIterable<T> checkContains( final Predicate<T> predicate, final Error error ) {
		final CheckingIterable<T> parentChecker = this;
		return new CheckingIterable<T>() {

			MatchCountingIterator<T> mci;

			@Override
			public Iterator<T> iterator() {
				return mci = new MatchCountingIterator<T>( parentChecker.iterator(), predicate );
			}

			@Override
			protected void close() {
				parentChecker.close();
				if ( mci.getCount() == 0L ) {
					throw error;
				}
			}

		};
	}

	/**
	 * Build a CheckingIterable which checks that the collection contains at least one element for which the given predicate is true.
	 * @param predicate the condition to look for
	 * @param collectionSpec how the collection description is to be phrased in the error message
	 * @param matchingObjectSpec how the matching object description is to be phrased in the error message
	 * @return this CheckingIterable wrapped to check the condition
	 */
	public CheckingIterable<T> checkContainsOne( final Predicate<T> predicate, final String collectionSpec, final String matchingObjectSpec ) {
		final CheckingIterable<T> parentChecker = this;
		return new CheckingIterable<T>() {

			MatchCountingIterator<T> mci;

			@Override
			public Iterator<T> iterator() {
				return mci = new MatchCountingIterator<T>( parentChecker.iterator(), predicate );
			}

			@Override
			protected void close() {
				parentChecker.close();
				final long cnt = mci.getCount();
				if ( cnt == 0L ) {
					throw new AssertionError( collectionSpec + " does not contain " + matchingObjectSpec );
				}
				if ( cnt > 1L ) {
					throw new AssertionError( collectionSpec + " contains " + cnt + " instances of " + matchingObjectSpec );
				}
			}

		};
	}

	private static class MatchCountingIterator<T> implements Iterator<T> {
		private final Predicate<T> predicate;
		private final Iterator<T> parent;
		private long count = 0;

		MatchCountingIterator( final Iterator<T> parent, final Predicate<T> predicate ) {
			this.predicate = predicate;
			this.parent = parent;
		}

		@Override
		public boolean hasNext() {
			return parent.hasNext();
		}

		@Override
		public T next() {
			final T obj = parent.next();
			if ( predicate.test( obj ) ) {
				++count;
			}
			return obj;
		}

		public long getCount() {
			return count;
		}

	}

	/**
	 * Build a CheckingIterable which checks that the given predicate is true for all elements of the collection.
	 * @param predicate the condition to look for
	 * @param message to signal when no matching element is found
	 * @return this CheckingIterable wrapped to check the condition
	 */
	public CheckingIterable<T> checkForAll( final Predicate<T> predicate, final String message ) {
		final CheckingIterable<T> parentChecker = this;
		return new CheckingIterable<T>() {

			@Override
			public Iterator<T> iterator() {
				final Iterator<T> parentIterator = parentChecker.iterator();
				return new Iterator<T>() {

					@Override
					public boolean hasNext() {
						return parentIterator.hasNext();
					}

					@Override
					public T next() {
						final T obj = parentIterator.next();
						final boolean match = predicate.test( obj );
						if ( ! match ) {
							throw new AssertionFailedError( message + "; object: " + obj );
						}
						return obj;
					}

				};
			}

			@Override
			protected void close() {
				parentChecker.close();
			}

		};
	}

	/**
	 * Build a CheckingIterable which checks that two values are equal for all elements of the collection.
	 * @param expectedFunction the function to construct the expected value
	 * @param realFunction the function extract the actual value
	 * @param message the message to signal when the actual value is not equal to the expected one
	 * @return this CheckingIterable wrapped to check the condition
	 */
	public <U> CheckingIterable<T> checkForAllEquals( final Function<T, U> expectedFunction, final Function<T, U> realFunction, final String message ) {
		return checkForAll( new Predicate<T>() {

			@Override
			public boolean test( final T obj ) {
				final U expectedValue = expectedFunction.apply( obj );
				final U realValue = realFunction.apply( obj );
				assertEquals( message, expectedValue, realValue );
				return true;
			}
			
		}, null );
	}

	/**
	 * Build a CheckingIterable which checks that a derived value belongs to a derived set for all elements of the collection.
	 * @param expectedFunction the function to construct the set of expected values
	 * @param realFunction the function extract the actual value
	 * @param message the message to signal when the actual value does not belong to the expected set
	 * @return this CheckingIterable wrapped to check the condition
	 */
    public <U> CheckingIterable<T> checkForAllValueInSet( final Function<T, Set<U>> expectedFunction, final Function<T, U> realFunction, final String message ) {
        return checkForAll( new Predicate<T>()
        {

            @Override
            public boolean test(final T obj)
            {
                final Set<U> expectedValues = expectedFunction.apply( obj );
                final U realValue = realFunction.apply( obj );
                assertTrue( message + "; object: " + obj, expectedValues.contains( realValue ) );
                return true;
            }

        }, null );
    }
	   
	/**
	 * Build a CheckingIterable which checks that the values of a function for all elements of the collection are unique.
	 * @param function the mapping from the collection element to the key that should be unique
	 * @param message the message to write on a non-unique key
	 * @return this CheckingIterable wrapped to check the condition
	 */
	public <U> CheckingIterable<T> checkUnique( final Function<T, U> function, final String message ) {
		final CheckingIterable<T> parentChecker = this;
		final Set<U> seenValues = new HashSet<>();
		return new CheckingIterable<T>() {

			@Override
			public Iterator<T> iterator() {
				final Iterator<T> parentIterator = parentChecker.iterator();
				return new Iterator<T>() {

					@Override
					public boolean hasNext() {
						return parentIterator.hasNext();
					}

					@Override
					public T next() {
						final T obj = parentIterator.next();
						final U val = function.apply( obj );
						if ( val != null && !seenValues.add( val ) ) {
							throw new AssertionFailedError( message + "; value: " + val );
						}
						return obj;
					}

				};
			}

			protected void close() {
				parentChecker.close();
			}

		};
	}

}
