/**
 * <copyright>
 * </copyright>
 *
 * $Id: ReplyFactory.java 1 2006-06-12 17:37:42Z sxp $
 */
package edu.tsinghua.lumaqq.ecore.reply;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see edu.tsinghua.lumaqq.ecore.reply.ReplyPackage
 * @generated
 */
public interface ReplyFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ReplyFactory eINSTANCE = edu.tsinghua.lumaqq.ecore.reply.impl.ReplyFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Replies</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Replies</em>'.
	 * @generated
	 */
	Replies createReplies();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	ReplyPackage getReplyPackage();

} //ReplyFactory
