/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.mobicents.tools.twiddle.jslee;

import java.io.PrintWriter;

import javax.management.ObjectName;

/**
 * @author baranowb
 *
 */
public class ProfileUsageCommand extends AbstractUsageCommand {

	/**
	 * 
	 */
	public ProfileUsageCommand() {
		super("usage.profile",
				"This command performs operations on JSLEE MBeans which are associated with usage parameter sets - be it specific MBean for parameter set, notifiaction management MBean...");
	}

	/* (non-Javadoc)
	 * @see org.mobicents.tools.twiddle.jslee.AbstractUsageCommand#getProvisioningMBeanName()
	 */
	@Override
	public ObjectName getProvisioningMBeanName() {
		return super.PROFILE_PROVISIONING_MBEAN;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.tools.twiddle.jslee.AbstractUsageCommand#getUsageMGMTMBeanOperation()
	 */
	@Override
	public String getUsageMGMTMBeanOperation() {
		
		return PROFILE_GET_METHOD;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.tools.twiddle.jslee.AbstractUsageCommand#addExamples()
	 */
	@Override
	protected void addExamples(PrintWriter out) {
		out.println("");
		out.println("     1. List usage parameters type for profile table:");
		out.println("" + name + " CallControlTableName -l --parameters");
		out.println("");
		out.println("     2. List existing usage parameters sets:");
		out.println("" + name + " CallControlTableName -l --sets");
		out.println("");
		out.println("     3. Get value of parameter in certain set:");
		out.println("" + name + " CallControlTableName CertainSetWithValue -g --name=CookiesCount");
		out.println("");
		out.println("     4. Get value of parameter in certain set and reset value:");
		out.println("" + name + " CallControlTableName CertainSetWithValue -g --name=CookiesCount --rst");
		out.println("");
		out.println("     5. Reset all parameters in default parameter set of table:");
		out.println("" + name + " CallControlTableName --reset");
		out.println("");
		out.println("     6. Create parameter set:");
		out.println("" + name + " CallControlTableName NewSet --create");
		out.println("");
		out.println("     7. Enable notification generation for parameter:");
		out.println("" + name + " CallControlTableName -n --name=CookiesCount --value=true");
		
		
		
	}

	/* (non-Javadoc)
	 * @see org.mobicents.tools.twiddle.jslee.AbstractUsageCommand#addHeaderDescription()
	 */
	@Override
	protected void addHeaderDescription(PrintWriter out) {
		out.println("usage: " + name + " <ProfileTableName> [SetName] <-operation[[arg] | [--option[=arg]]*]>");
	}

}
