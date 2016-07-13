/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jivesoftware.smackx.filetransfer;

import org.jivesoftware.smackx.packet.StreamInitiation;

/**
 * A request to send a file recieved from another user.
 * 
 * @author Alexander Wenckus
 * 
 */
public class FileTransferRequest {
	private final StreamInitiation streamInitiation;

	private final FileTransferManager manager;

	/**
	 * A recieve request is constructed from the Stream Initiation request
	 * received from the initator.
	 * 
	 * @param manager
	 *            The manager handling this file transfer
	 * 
	 * @param si
	 *            The Stream initiaton recieved from the initiator.
	 */
	public FileTransferRequest(FileTransferManager manager, StreamInitiation si) {
		this.streamInitiation = si;
		this.manager = manager;
	}

	/**
	 * Returns the name of the file.
	 * 
	 * @return Returns the name of the file.
	 */
	public String getFileName() {
		return streamInitiation.getFile().getName();
	}

	/**
	 * Returns the size in bytes of the file.
	 * 
	 * @return Returns the size in bytes of the file.
	 */
	public long getFileSize() {
		return streamInitiation.getFile().getSize();
	}

	/**
	 * Returns the description of the file provided by the requestor.
	 * 
	 * @return Returns the description of the file provided by the requestor.
	 */
	public String getDescription() {
		return streamInitiation.getFile().getDesc();
	}

	/**
	 * Returns the mime-type of the file.
	 * 
	 * @return Returns the mime-type of the file.
	 */
	public String getMimeType() {
		return streamInitiation.getMimeType();
	}

	/**
	 * Returns the fully-qualified jabber ID of the user that requested this
	 * file transfer.
	 * 
	 * @return Returns the fully-qualified jabber ID of the user that requested
	 *         this file transfer.
	 */
	public String getRequestor() {
		return streamInitiation.getFrom();
	}

	/**
	 * Returns the stream ID that uniquely identifies this file transfer.
	 * 
	 * @return Returns the stream ID that uniquely identifies this file
	 *         transfer.
	 */
	public String getStreamID() {
		return streamInitiation.getSessionID();
	}

	/**
	 * Returns the stream initiation packet that was sent by the requestor which
	 * contains the parameters of the file transfer being transfer and also the
	 * methods available to transfer the file.
	 * 
	 * @return Returns the stream initiation packet that was sent by the
	 *         requestor which contains the parameters of the file transfer
	 *         being transfer and also the methods available to transfer the
	 *         file.
	 */
	protected StreamInitiation getStreamInitiation() {
		return streamInitiation;
	}

	/**
	 * Accepts this file transfer and creates the incoming file transfer.
	 * 
	 * @return Returns the <b><i>IncomingFileTransfer</b></i> on which the
	 *         file transfer can be carried out.
	 */
	public IncomingFileTransfer accept() {
		return manager.createIncomingFileTransfer(this);
	}

	/**
	 * Rejects the file transfer request.
	 */
	public void reject() {
		manager.rejectIncomingFileTransfer(this);
	}

}
