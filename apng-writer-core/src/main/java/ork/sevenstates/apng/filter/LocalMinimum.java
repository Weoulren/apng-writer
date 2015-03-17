package ork.sevenstates.apng.filter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

abstract class LocalMinimum extends Filter {
	private final Filter[] simpleFilters = {
			new None(),
			new Sub(), 
			new Up(), 
			new Average(), 
			new Paeth()
	};
	
	private ExecutorService tpe = new ForkJoinPool();

	@Override
	public void setBpp(int bpp) {
		super.setBpp(bpp);
		for (int i = 0; i < simpleFilters.length; i++) {
			simpleFilters[i].setBpp(bpp);
		}
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		for (int i = 0; i < simpleFilters.length; i++) {
			simpleFilters[i].setHeight(height);
		}
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		for (int i = 0; i < simpleFilters.length; i++) {
			simpleFilters[i].setWidth(width);
		}
	}

	@Override
	public void close() {
		tpe.shutdown();
		super.close();
	}

	@Override
	protected void encodeRow(final ByteBuffer in, final int srcOffset, ByteBuffer out, final int len,
			int destOffset) {
		Integer minsize = null;
		List<Callable<Entry<Integer, ByteBuffer>>> tasks = new ArrayList<>();
		
		for (int i = 0; i < simpleFilters.length; i++) {
			final Filter filter = simpleFilters[i];
			tasks.add(new Callable<Map.Entry<Integer,ByteBuffer>>() {
				
				@Override
				public Entry<Integer, ByteBuffer> call() {
					return doCall(in, srcOffset, len, filter);
				}
			});
		}

		List<Future<Entry<Integer, ByteBuffer>>> results = null;
		try {
			results = tpe.invokeAll(tasks);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Entry<Integer, ByteBuffer> min = null;
		for (int i = 0; i < simpleFilters.length; i++) {
			Entry<Integer, ByteBuffer> e = min;
			try {
				e = results.get(i).get();
			} catch (InterruptedException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (ExecutionException ex) {
				// doCall throws no exception, anything here is not something we'd expect
				throw new RuntimeException(ex);
			}
			// e = doCall(in, srcOffset, len, filter[i]);
			if (minsize == null || e.getKey() < minsize) {
				minsize = e.getKey();
				min = e;
			}
		}

		out.position(destOffset);
		out.put(min.getValue());
	}
	
	protected abstract Entry<Integer, ByteBuffer> doCall(ByteBuffer src, int srcOffset, int len, Filter f);
}
