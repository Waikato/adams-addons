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

/**
 * Upload.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.webservice.text;

import java.net.URL;

import javax.xml.ws.BindingProvider;

import nz.ac.waikato.adams.webservice.rats.text.RatsTextService;
import nz.ac.waikato.adams.webservice.rats.text.RatsTextServiceService;
import nz.ac.waikato.adams.webservice.rats.text.UploadRequest;
import nz.ac.waikato.adams.webservice.rats.text.UploadResponse;
import adams.data.text.TextContainer;
import adams.flow.core.RatsTextHelper;
import adams.flow.webservice.AbstractWebServiceClientSink;
import adams.flow.webservice.WebserviceUtils;

/**
 * Uploads a TextContainer.
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2085 $
 */
public class Upload 
  extends AbstractWebServiceClientSink<TextContainer>{

  /** for serialization*/
  private static final long serialVersionUID = -338043583699608760L;
  
  /** input container */
  protected TextContainer m_ContainerIn;

  /** the format. */
  protected String m_Format;
  
  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Stores a TextContainer using the RATS text webservice.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "format", "format",
	    WebserviceUtils.MIMETYPE_PLAIN_TEXT);
  }
  
  /**
   * Sets the text format.
   *
   * @param value	the format
   */
  public void setFormat(String value) {
    m_Format = value;
    reset();
  }

  /**
   * Returns the text format.
   *
   * @return		the format
   */
  public String getFormat() {
    return m_Format;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String formatTipText() {
    return "The text format type.";
  }

  /**
   * Returns the classes that are accepted input.
   * 
   * @return		the classes that are accepted
   */
  @Override
  public Class[] accepts() {
    return new Class[]{TextContainer.class};
  }

  /**
   * Returns the WSDL location.
   * 
   * @return		the location
   */
  @Override
  public URL getWsdlLocation() {
    return getClass().getClassLoader().getResource("wsdl/adams/RatsTextService.wsdl");
  }

  /**
   * Sets the data for the request, if any.
   * 
   * @param value	the request data
   */
  @Override
  public void setRequestData(TextContainer value) {
    m_ContainerIn = value;
  }

  /**
   * Performs the actual webservice query.
   * 
   * @throws Exception	if accessing webservice fails for some reason
   */
  @Override
  protected void doQuery() throws Exception {
    RatsTextServiceService ratsServiceService;
    RatsTextService ratsService;
    ratsServiceService = new RatsTextServiceService(getWsdlLocation());
    ratsService = ratsServiceService.getRatsTextServicePort();
    WebserviceUtils.configureClient(ratsService, m_ConnectionTimeout, m_ReceiveTimeout, getUseAlternativeURL() ? getAlternativeURL() : null);
    //check against schema
    WebserviceUtils.enableSchemaValidation(((BindingProvider) ratsService));
   
    UploadRequest request = new UploadRequest();
    request.setId(m_ContainerIn.getID());
    request.setFormat(m_Format);
    request.setText(RatsTextHelper.containerToWebservice(m_ContainerIn));
    UploadResponse response = ratsService.upload(request);
    
    // failed to generate data?
    if (!response.isSuccess())
      throw new IllegalStateException(response.getMessage());
  }
}
