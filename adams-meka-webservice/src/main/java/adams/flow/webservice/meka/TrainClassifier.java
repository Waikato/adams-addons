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
 * TrainClassifier.java
 * Copyright (C) 2013-2019 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.webservice.meka;

import adams.flow.webservice.AbstractWebServiceClientTransformer;
import adams.flow.webservice.WebserviceUtils;
import nz.ac.waikato.adams.webservice.meka.MekaService;
import nz.ac.waikato.adams.webservice.meka.MekaServiceService;
import nz.ac.waikato.adams.webservice.meka.TrainClassifierResponseObject;

import javax.xml.ws.BindingProvider;
import java.net.URL;

/**
 * client for using the training webservice.
 * 
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class TrainClassifier 
extends AbstractWebServiceClientTransformer<nz.ac.waikato.adams.webservice.meka.TrainClassifier, String> {

  /** for serialization*/
  private static final long serialVersionUID = 1L;

  /** input object for the train webservice */
  protected nz.ac.waikato.adams.webservice.meka.TrainClassifier m_Train;

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
    return "Train a classifier on a dataset and returns whether it was succesful.";
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
   * Returns the classes that are accepted input.
   * 
   * @return		the classes that are accepted
   */
  @Override
  public Class[] accepts() {
    return new Class[] {nz.ac.waikato.adams.webservice.meka.TrainClassifier.class};
  }

  /**
   * Sets the data for the request, if any.
   * 
   * @param value	the request data
   */
  @Override
  public void setRequestData(nz.ac.waikato.adams.webservice.meka.TrainClassifier value) {
    m_Train = value;
  }

  /**
   * Returns the classes that this client generates.
   * 
   * @return		the classes
   */
  @Override
  public Class[] generates() {
    return new Class[] {String.class};
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
        m_OutInterceptor);
      //check against schema
      WebserviceUtils.enableSchemaValidation(((BindingProvider) m_Port));
    }
    TrainClassifierResponseObject response = m_Port.trainClassifier(m_Train.getDataset(), m_Train.getClassifier(), m_Train.getName());
    if (response.getErrorMessage() != null)
      throw new IllegalStateException(response.getErrorMessage());
    setResponseData(response.getModel());
    m_Train = null;
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
