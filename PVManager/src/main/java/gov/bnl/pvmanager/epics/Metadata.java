/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.pvmanager.epics;

import java.lang.annotation.Documented;

/**
 * Annotation to flag which fields are considered part of the metadata.
 * In Epics V3, these fields are fetched once at each connection, while
 * in Epics V5 are monitored as the rest.
 * 
 * @author carcassi
 */
@Documented
public @interface Metadata {

}
