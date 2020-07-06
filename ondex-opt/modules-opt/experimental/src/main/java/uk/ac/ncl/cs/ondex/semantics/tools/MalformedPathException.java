/*
 * OndexView plug-in for Cytoscape
 * Copyright (C) 2010  University of Newcastle upon Tyne
 * 
 * This file is part of OndexView.
 * 
 * OndexView is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OndexView is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with OndexView.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ncl.cs.ondex.semantics.tools;

/**
 * thrown if a metagraph path descriptor is invalid.
 * @author jweile
 *
 */
public class MalformedPathException extends Exception {

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = 1L;

	public MalformedPathException() {
	}

	public MalformedPathException(String message) {
		super(message);
	}

	public MalformedPathException(Throwable cause) {
		super(cause);
	}

	public MalformedPathException(String message, Throwable cause) {
		super(message, cause);
	}

}
