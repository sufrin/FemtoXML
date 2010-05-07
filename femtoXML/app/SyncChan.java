package femtoXML.app;

/** Synchronous channel */
public class SyncChan<T>
{
	public static class Closed extends Error
	{
	}

	T buf, bufN;
	boolean readDone, readWaiting, writeWaiting, isOpen = true, bufSet;

	public synchronized void close()
	{
		isOpen = false;
		// interrupt any waiting operation
		if (readWaiting || writeWaiting)
			notify();
	}

	public synchronized void write(T t)
	{
		if (!isOpen)
			throw new Closed();
		readDone = false;
		if (readWaiting)
		{
			readWaiting = false;
			buf = t;
			notify();
		} else
		{
			writeWaiting = true;
			buf = t;
		}
		// guard against phantom notifications
		while (!readDone && isOpen)
			try
			{
				wait();
			} catch (InterruptedException e)
			{
			}
		if (!isOpen)
			throw new Closed();
	}

	public synchronized boolean hasNext()
	{
		if (bufSet)
			return true;
		if (writeWaiting)
			writeWaiting = false;
		else
		{
			readWaiting = true;
			// await a write or a close
			while (readWaiting && isOpen)
				try
				{
					wait();
				} catch (InterruptedException e)
				{
				}
			// !isOpen || !readWaiting
			if (!isOpen)
				return false;
		}
		readDone = true;
		bufN = buf;
		bufSet = true;
		notify();
		return true;
	}

	public synchronized T next()
	{
		if (bufSet)
		{
			bufSet = false;
			return bufN;
		} else
			throw new IllegalStateException();
	}

}
