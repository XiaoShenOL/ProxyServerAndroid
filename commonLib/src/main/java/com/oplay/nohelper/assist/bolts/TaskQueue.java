package com.oplay.nohelper.assist.bolts;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * task排列的管理类
 * 支持多线程操作,可以入列,但这里的入列不是在前个任务完成的情况下入列,你什么时候入列就开始执行,应该是
 * 任务之前是没有关系的, 即 这里的 then(Task<Void> task) 中的task 不起作用
 * <p/>
 * tasks可以等待某个task完成的情况下才能进行下面的操作.
 * <p/>
 * 等待所有的任务全部完成.
 *
 * @author zyq 15-10-20
 */
public class TaskQueue {

	/**
	 * We only need to keep the tail of the queue.Cancelled tasks will just complete
	 * normally/immediately when their turn arrives;
	 */
	private Task<Void> tail;
	private final Lock lock = new ReentrantLock();

	/**
	 * 相当于全部任务完成后的一个代理
	 * Gets a task that can be safely awaited and is dependent on the current tail of the queue. This
	 * essentially gives us a proxy for the tail end of the queue that can be safely cancelled.
	 *
	 * @return A new task that should be awaited by enqueued tasks.
	 */
	private Task<Void> getTaskToAwait() {
		lock.lock();
		try {
			//队列完成后返回的是Task<Void>
			//tail != null 表明当前队列还在进行中,当有任务时候
			Task<Void> toAwait = tail != null ? tail : Task.<Void>forResult(null);
			//等待直到队尾最后一个任务完成,返回Task<Void>
			return toAwait.continueWith(new Continuation<Void, Void>() {
				@Override
				public Void then(Task<Void> task) throws Exception {
					return null;
				}
			});
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 入队
	 * Enqueues a task created by taskStart.
	 *
	 * @param taskStart A function given a task to await once state is snapshotted (e.g. after capturing
	 *                  session tokens at the time of the save call). Awaiting this task will wait for the
	 *                  created task's turn in the queue.
	 * @return The task created by the taskStart function.
	 */
	<T> Task<T> enqueue(Continuation<Void, Task<T>> taskStart) {
		lock.lock();
		try {
			Task<T> task;
			Task<Void> oldTail = tail != null ? tail : Task.<Void>forResult(null);
			// The task created by taskStart is responsible for waiting for the task passed into it before
			// doing its work (this gives it an opportunity to do startup work or save state before
			// waiting for its turn in the queue)
			try {
				Task<Void> toAwait = getTaskToAwait();//这里没有堵塞
				task = taskStart.then(toAwait);//toAwait这时候不一定是完成的
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			// The tail task should be dependent on the old tail as well as the newly-created task. This
			// prevents cancellation of the new task from causing the queue to run out of order.
			// 当全部任务完成的情况下,tail.getResult() == null;
			tail = Task.whenAll(Arrays.asList(oldTail, task));
			return task;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 指的是一个队列若想进行下个任务,必须要把 toAwait 这个任务 执行完才能继续.
	 * Creates a continuation that will wait for the given task to complete before running the next
	 * continuations.
	 */
	static <T> Continuation<T, Task<T>> waitFor(final Task<Void> toAwait) {
		return new Continuation<T, Task<T>>() {
			@Override
			public Task<T> then(final Task<T> task) throws Exception {
				return toAwait.continueWithTask(new Continuation<Void, Task<T>>() {
					@Override
					public Task<T> then(Task<Void> ignored) throws Exception {
						return task;
					}
				});
			}
		};
	}

	Lock getLock() {
		return lock;
	}

	/**
	 * 等到全部的任务全部完成
	 *
	 * @throws InterruptedException
	 */
	void waitUntilFInished() throws InterruptedException {
		lock.lock();
		try {
			if (tail == null)
				return;
			tail.waitForCompletion();
		} finally {
			lock.unlock();
		}
	}
}
