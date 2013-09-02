/*******************************************************************************
 * Copyright 2013
 * Telecooperation (TK) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.da_sense.moses.client.abstraction.apks;

import java.io.File;

public interface ApkDownloadObserver {
	/**
	 * Notifies the observer, that the download was finished. if the reference
	 * to the external application was set, it will be passed on as parameter,
	 * otherwise this parameter will be null.
	 * 
	 * @param downloader
	 *            the downloader which performed the download
	 * @param result
	 *            the file reference to the downloaded apk file
	 * @param externalAppRef
	 *            the external application reference, if it was set via
	 *            {@link ApkDownloadTask#setExternalApplicationReference(ExternalApplication)}
	 */
	public void apkDownloadFinished(ApkDownloadTask downloader, File result, ExternalApplication externalAppRef);

	/**
	 * Notifies the observer that the download failed; the exception that was
	 * thrown can be retrieved via
	 * {@link ApkDownloadTask#getDownloadException()}
	 * 
	 * @param downloader
	 */
	public void apkDownloadFailed(ApkDownloadTask downloader);
}
