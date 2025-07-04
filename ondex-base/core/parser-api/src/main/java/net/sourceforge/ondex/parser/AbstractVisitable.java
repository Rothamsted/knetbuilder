package net.sourceforge.ondex.parser;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * TODO: Never used! It's just an experiment!
 *
 * @author Marco Brandizi
 * <dl><dt>Date:</dt><dd>4 Jul 2025</dd></dl>
 *
 */
public abstract class AbstractVisitable<T> implements Visitable<T>
{
	private Set<T> visitedValues = new HashSet<> ();
	private Visitable<T> delegate;
	
	@Override
	public boolean isVisited ( T value )
	{
		if ( delegate != null ) return delegate.isVisited ( value );
		return visitedValues.contains ( value );
	}

	@Override
	public boolean setVisited ( T value, boolean isVisited )
	{
		if ( delegate != null ) return delegate.setVisited ( value );
		
		if ( isVisited ) return !visitedValues.add ( value );
		return visitedValues.remove ( visitedValues );
	}
	
	@Override
	public boolean setVisited ( T value )
	{
		if ( delegate != null ) return delegate.setVisited ( value );
		return Visitable.super.setVisited ( value );
	}

	@Override
	@SuppressWarnings ( "unchecked" )
	public void setDelegateVisitable ( Visitable<? extends T> delegate )
	{
		Objects.requireNonNull ( delegate, "Can't set a null delegate Visitable" );
		this.delegate = (Visitable<T>) delegate;
	}
}
