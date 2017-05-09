/**
 *    Copyright 2015-2017 donghyuck
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package architecture.community.attachment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import architecture.community.attachment.dao.AttachmentDao;
import architecture.community.exception.NotFoundException;
import architecture.community.user.User;
import architecture.community.util.SecurityHelper;
import architecture.ee.exception.RuntimeError;
import architecture.ee.service.Repository;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

public class DefaultAttachmentService extends AbstractAttachmentService implements AttachmentService {

	
	@Inject
	@Qualifier("repository")
	private Repository repository;

	@Inject
	@Qualifier("attachmentDao")
	private AttachmentDao attachmentDao;

	@Inject
	@Qualifier("attachmentCache")
	private Cache attachmentCache;
	
	private File attachmentDir;		
	
	
	public DefaultAttachmentService() {
	}

	

	protected synchronized File getAttachmentDir() {
		if(attachmentDir == null)
        {
			attachmentDir = repository.getFile("attachments");
			if(!attachmentDir.exists())
            {
                boolean result = attachmentDir.mkdir();
                if(!result)
                    log.error((new StringBuilder()).append("Unable to create attachment directory: '").append(attachmentDir).append("'").toString());
            }
        }
        return attachmentDir;
	}
	

	public Attachment getAttachment(long attachmentId) throws NotFoundException {
		
		Attachment attachment = getAttachmentInCache(attachmentId);
		if( attachment == null){
			attachment = attachmentDao.getByAttachmentId(attachmentId);
			attachmentCache.put(new Element(attachmentId, attachment ));
		}
		return attachment;
	}

	public List<Attachment> getAttachments(int objectType, long objectId) {
		
		List<Long> ids = attachmentDao.getAttachmentIds(objectType, objectId);		
		List<Attachment> list = new ArrayList<Attachment>(ids.size());
		for( Long id : ids){
			try {
				list.add(getAttachment(id));
			} catch (NotFoundException e) {
				log.error(e.getMessage(), e);
			}
		}		
		return list;
		
		//return attachmentDao.getByObjectTypeAndObjectId(objectType, objectId);
	
	}
	
	protected Attachment getAttachmentInCache(long attachmentId){
		if( attachmentCache.get(attachmentId) != null && attachmentId > 0L )
			return  (Attachment) attachmentCache.get( attachmentId ).getObjectValue();
		else 
			return null;
	}
	
	
	public Attachment createAttachment(int objectType, long objectId, String name, String contentType, File file) {
		
		
		DefaultAttachment attachment = new DefaultAttachment();
		attachment.setObjectType(objectType);
		attachment.setObjectId(objectId);
		attachment.setName(name);
		attachment.setContentType(contentType);		
		attachment.setSize( (int) FileUtils.sizeOf(file));
		try {
			attachment.setInputStream(FileUtils.openInputStream(file));
		} catch (IOException e) {
			log.debug(e.getMessage(), e);
		}	
		return attachment;
	}
	
	public Attachment createAttachment(int objectType, long objectId, String name, String contentType, InputStream inputStream) {
		
		DefaultAttachment attachment = new DefaultAttachment();
		attachment.setObjectType(objectType);
		attachment.setObjectId(objectId);
		attachment.setName(name);
		attachment.setContentType(contentType);
		attachment.setInputStream(inputStream);		
		try {
			attachment.setSize( IOUtils.toByteArray(inputStream).length );
		} catch (IOException e) {
			log.debug(e.getMessage(), e);
		}		
		return attachment;
	}
	
	
	public long getUserId() {
		try {
			User user = SecurityHelper.getUser();
			return user.getUserId();			
		} catch (Exception ignore) {
		}			
		return -1L;
	}
	
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW )
	public Attachment saveAttachment(Attachment attachment) {
		
		long userId = getUserId();
	
		Date now = new Date();
		Attachment attachmentToUse = attachment ;
		if( attachmentToUse.getAttachmentId() > 0 ){
			attachmentCache.remove(attachmentToUse.getAttachmentId());			
			attachmentToUse.setModifiedDate(now);
			attachmentDao.updateAttachment(attachmentToUse);	
			
			if( attachmentCache.get(attachmentToUse.getAttachmentId()) != null ){
				attachmentCache.remove(attachmentToUse.getAttachmentId());
			}
			
		}else{			
			attachmentToUse.setCreationDate(now);
			attachmentToUse.setModifiedDate(now);
			((DefaultAttachment)attachmentToUse).setUserId(userId);
			attachmentToUse = attachmentDao.createAttachment(attachmentToUse);
		}		
		try {
			
			if( attachmentToUse.getInputStream() != null )
			{
				attachmentDao.saveAttachmentData(attachmentToUse, attachmentToUse.getInputStream());				
				Collection<File> files = FileUtils.listFiles(getAttachmentCacheDir(), FileFilterUtils.prefixFileFilter(attachment.getAttachmentId() + ""), null);
				for(File file : files){
					FileUtils.deleteQuietly(file);
				}				
			}
			
			return getAttachment(attachment.getAttachmentId());
		} catch (Exception e) {
			throw new RuntimeError(e);
		}
	}

	
	public InputStream getAttachmentInputStream(Attachment attachment) {
		try {
			File file = getAttachmentFromCacheIfExist(attachment);
			return FileUtils.openInputStream(file);
		} catch (IOException e) {
			throw new RuntimeError(e);
		}
	}
	
	protected File getAttachmentFromCacheIfExist(Attachment attachment) throws IOException{		
		File dir = getAttachmentCacheDir();
		
		StringBuilder sb = new StringBuilder();
		sb.append( attachment.getAttachmentId() ).append(".bin");		
		
		File file = new File(dir, sb.toString() );		
		if( file.exists() ){
			long size = FileUtils.sizeOf(file);
			if( size != attachment.getSize() ){
				// size different make cache new one....
				InputStream inputStream = attachmentDao.getAttachmentData(attachment);
				FileUtils.copyInputStreamToFile(inputStream, file);
			}
		}else{
			// doesn't exist, make new one ..
			InputStream inputStream = attachmentDao.getAttachmentData(attachment);
			FileUtils.copyInputStreamToFile(inputStream, file);
		}		
		return file;
	}	
	
	protected File getAttachmentCacheDir(){
		File dir = new File(getAttachmentDir(), "cache" );	
		return dir;
	}
	

	public void  initialize() {		
		log.debug( "initializing attachement manager" );		
		getAttachmentDir();
	}	
	public void destroy(){
		
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW )
	public void removeAttachment(Attachment attachment) {		
		Attachment attachmentToUse = attachment ;		
		if( attachmentToUse.getAttachmentId() > 0 ){
			attachmentCache.remove(attachmentToUse.getAttachmentId());		
			attachmentDao.deleteAttachment(attachmentToUse);
			attachmentDao.deleteAttachmentData(attachmentToUse);
		}
	}

	public void move(int objectType, long objectId, int targetObjectType, long targetObjectId) {
		List<Long> ids = attachmentDao.getAttachmentIds(objectType, objectId);
		if(ids.size() > 0){
			for(Long id : ids ){
				if(attachmentCache.get(id)!=null)
					attachmentCache.remove(id);		
			}
			attachmentDao.move(objectType, objectId, targetObjectType, targetObjectId);
		}
	}

}