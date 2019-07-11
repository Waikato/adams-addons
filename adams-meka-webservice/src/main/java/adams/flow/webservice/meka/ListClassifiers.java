/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * ListClassifiers.java
 * Copyright (C) 2013-2019 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.webservice.meka;

import adams.flow.webservice.AbstractWebServiceClientSource;
import adams.flow.webservice.WebserviceUtils;
import nz.ac.waikato.adams.webservice.meka.MekaService;
import nz.ac.waikato.adams.webservice.meka.MekaServiceService;

import javax.xml.ws.BindingProvider;
import java.net.URL;
import java.util.ArrayList;

/**
 * Lists all currently available classifier models.
 * 
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class ListClassifiers 
extends AbstractWebServiceClientSource<ArrayList<String>> {

  /** for serialization*/
  private static final long serialVersionUID = 6494416312486305534L;

  /** the service instance. */
  protected transient MekaServiceService m_Service;

  /** the port instance. */
  protected transient MekaService m_Port;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "displays a list of all the classifiers currently stored";
  }

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();

    m_Service = null;
    m_Port    = null;
  }

  /**
   * Returns the classes that this client generates.
   * 
   * @return		the classes
   */
  @Override
  public Class[] generates() {
    return new Class[] {ArrayList.class};
  }

  /**
   * Returns the WSDL location.
   * 
   * @return		the location
   */
  @Override
  public URL getWsdlLocation() {
    return getClass().getClassLoader().getResource("wsdl/meka/MekaService.wsdl");

  }

  /**
   * Performs the actual webservice query.
   * 
   * @throws Exception	if accessing webservice fails for some reason
   */
  @Override
  protected void doQuery() throws Exception {
    if (m_Service == null) {
      m_Service = new MekaServiceService(getWsdlLocation());
      m_Port = m_Service.getMekaServicePort();
      WebserviceUtils.configureClient(
	m_Owner,
	m_Port,
	m_ConnectionTimeout,
	m_ReceiveTimeout,
	(getUseAlternativeURL() ? getAlternativeURL() : null),
	m_InInterceptor,
	null);
      //check against schema
      WebserviceUtils.enableSchemaValidation(((BindingProvider) m_Port));
    }
    setResponseData((ArrayList<String>) m_Port.listClassifiers());
  }

  /**
   * Cleans up the client.
   */
  @Override
  public void cleanUp() {
    m_Service = null;
    m_Port    = null;

    super.cleanUp();
  }
}
