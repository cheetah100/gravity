/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Orcon Limited
 * 
 * This file is part of Gravity Workflow Automation.
 *
 * Gravity Workflow Automation is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Gravity Workflow Automation is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *    
 * You should have received a copy of the GNU General Public License
 * along with Gravity Workflow Automation.  
 * If not, see <http://www.gnu.org/licenses/>. 
 */

package nz.net.orcon.kanban.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractEncryptor implements Encryptor {

	private static Logger LOG = LoggerFactory.getLogger(AbstractEncryptor.class);

	private Encryptor encryptor;

	public AbstractEncryptor(Encryptor encryptor) {
		super();
		this.encryptor = encryptor;
	}

	@Override
	public byte[] encrypt(byte[] sourceIn) {
		LOG.debug("Encrypting with class:"+this.getClass().getName());
		final byte[] source = doEncrypt(sourceIn);
		return this.encryptor.encrypt(source);
	}

	public abstract byte[] doEncrypt(byte[] source) ;

	@Override
	public byte[] decrypt(byte[] cipherIn) {
		final byte[] cipher = this.encryptor.decrypt(cipherIn);
		LOG.debug("Decrypting with class:"+this.getClass().getName());
		return doDecrypt(cipher);
	}

	public abstract byte[] doDecrypt(byte[] cipher);

	public String toString(byte[] bytes){
		try {
			return new String(bytes, "UTF-8");
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid charset used for byte[] to String conversion.", e);
		}
	}

	public byte[] toBytes(String msg){
		try {
			return msg.getBytes("UTF-8");
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid charset used for String to byte[] conversion.", e);
		}
	}

	/**
	 * @return the encryptor
	 */
	public Encryptor getEncryptor() {
		return this.encryptor;
	}
	/**
	 * @param encryptor the encryptor to set
	 */
	public void setEncryptor(Encryptor encryptor) {
		this.encryptor = encryptor;
	}
}
