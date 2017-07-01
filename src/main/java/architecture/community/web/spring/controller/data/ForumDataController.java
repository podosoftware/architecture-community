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

package architecture.community.web.spring.controller.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import architecture.community.attachment.Attachment;
import architecture.community.attachment.AttachmentService;
import architecture.community.board.BoardNotFoundException;
import architecture.community.comment.Comment;
import architecture.community.comment.CommentService;
import architecture.community.exception.NotFoundException;
import architecture.community.exception.UnAuthorizedException;
import architecture.community.forum.DefaultForumMessage;
import architecture.community.forum.ForumMessage;
import architecture.community.forum.ForumMessageNotFoundException;
import architecture.community.forum.ForumService;
import architecture.community.forum.ForumThread;
import architecture.community.forum.ForumThreadNotFoundException;
import architecture.community.forum.MessageTreeWalker;
import architecture.community.image.ThumbnailImage;
import architecture.community.model.ModelObjectTreeWalker;
import architecture.community.model.ModelObjectTreeWalker.ObjectLoader;
import architecture.community.model.Models;
import architecture.community.user.User;
import architecture.community.util.SecurityHelper;
import architecture.community.web.model.ItemList;
import architecture.community.web.model.json.RequestData;
import architecture.community.web.model.json.Result;
import architecture.ee.util.StringUtils;

@Controller("forums-data-controller")
@RequestMapping("/data/forums")
public class ForumDataController {

	@Inject
	@Qualifier("forumService")
	private ForumService forumService;

	@Inject
	@Qualifier("commentService")
	private CommentService commentService;

	@Inject
	@Qualifier("attachmentService")
	private AttachmentService attachmentService;

	private Logger log = LoggerFactory.getLogger(ForumDataController.class);

	public ForumDataController() {
	}

	@Secured({ "ROLE_USER" })
	@RequestMapping(value = "/messages/add.json", method = { RequestMethod.POST })
	@ResponseBody
	public ForumMessage addThread(@RequestBody DefaultForumMessage newMessage, NativeWebRequest request) {

		User user = SecurityHelper.getUser();
		if (newMessage.getThreadId() < 1 && newMessage.getMessageId() < 1) {
			ForumMessage rootMessage = forumService.createMessage(newMessage.getObjectType(), newMessage.getObjectId(), user);
			rootMessage.setSubject(newMessage.getSubject());
			rootMessage.setBody(newMessage.getBody());
			ForumThread thread = forumService.createThread(rootMessage.getObjectType(), rootMessage.getObjectId(), rootMessage);
			forumService.addThread(rootMessage.getObjectType(), rootMessage.getObjectId(), thread);
			return thread.getRootMessage();
		}
		return newMessage;
	}

	@RequestMapping(value = "/message/{messageId:[\\p{Digit}]+}/get.json", method = RequestMethod.POST)
	@ResponseBody
	public ForumMessage getMessageById(@PathVariable Long messageId, NativeWebRequest request) throws NotFoundException {
		if (messageId < 1) {
			throw new ForumMessageNotFoundException();
		}
		ForumMessage message = forumService.getForumMessage(messageId);
		ForumThread thread = forumService.getForumThread(message.getThreadId());

		return message;
	}

	@RequestMapping(value = "/message/{messageId:[\\p{Digit}]+}/update.json", method = RequestMethod.POST)
	@ResponseBody
	public ForumMessage updateMessage(@RequestBody DefaultForumMessage newMessage, NativeWebRequest request) throws NotFoundException {
		
		User user = SecurityHelper.getUser();		
		
		ForumMessage message = forumService.getForumMessage(newMessage.getMessageId());
		message.setSubject(newMessage.getSubject());
		message.setBody(newMessage.getBody());
		forumService.updateMessage(message);
		
		return message;
	}	
	
	@RequestMapping(value = "/messages/{messageId:[\\p{Digit}]+}/attachments/list.json", method = RequestMethod.POST)
	@ResponseBody
	public ItemList listMessageFile(@PathVariable Long messageId, NativeWebRequest request) throws NotFoundException {

		ItemList list = new ItemList();
		if (messageId < 1) {
			return list;
		}

		ForumMessage message = forumService.getForumMessage(messageId);
		ForumThread thread = forumService.getForumThread(message.getThreadId());

		List<Attachment> attachments = attachmentService.getAttachments(Models.FORUM_MESSAGE.getObjectType(),
				message.getMessageId());
		list.setItems(attachments);
		list.setTotalCount(attachments.size());

		return list;

	}

	@RequestMapping(value = "/messages/{messageId:[\\p{Digit}]+}/attachments/{attachmentId:[\\p{Digit}]+}/{filename:.+}", method = { RequestMethod.GET, RequestMethod.POST })
	public void downloadMessageFile(
			@PathVariable Long messageId, 
			@PathVariable("attachmentId") Long attachmentId,
			@PathVariable("filename") String filename,
			@RequestParam(value = "thumbnail", defaultValue = "false", required = false) boolean thumbnail,
			@RequestParam(value = "width", defaultValue = "150", required = false) Integer width,
			@RequestParam(value = "height", defaultValue = "150", required = false) Integer height,
			HttpServletResponse response) throws NotFoundException, IOException {
		
		if (messageId > 0 && attachmentId > 0 && !StringUtils.isNullOrEmpty(filename)) {
			
			ForumMessage message = forumService.getForumMessage(messageId);
			ForumThread thread = forumService.getForumThread(message.getThreadId());
			Attachment attachment = attachmentService.getAttachment(attachmentId);
			
			if( thumbnail )
			{
				if( attachmentService.hasThumbnail(attachment) ){
					ThumbnailImage thumbnailImage = new ThumbnailImage();
					thumbnailImage.setWidth(width);
					thumbnailImage.setHeight(height);
					
					InputStream input = attachmentService.getAttachmentImageThumbnailInputStream(attachment, thumbnailImage );
					response.setContentType(thumbnailImage.getContentType());
				    response.setContentLength((int)thumbnailImage.getSize());
				    IOUtils.copy(input, response.getOutputStream());
				    response.flushBuffer();
				}else{
				    response.setStatus(301);
				    String url ="/images/no-image.jpg" ;
				    response.addHeader("Location", url);
				}
			} else {
				InputStream input = attachmentService.getAttachmentInputStream(attachment);
				response.setContentType(attachment.getContentType());
				response.setContentLength(attachment.getSize());
				IOUtils.copy(input, response.getOutputStream());
				response.setHeader("contentDisposition", "attachment;filename=" + getEncodedFileName(attachment));
				response.flushBuffer();					
			}
		}		
		throw new NotFoundException();
	}
	
    protected String getEncodedFileName(Attachment attachment) {
		try {
		    return URLEncoder.encode(attachment.getName(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
		    return attachment.getName();
		}
    }

	@Secured({ "ROLE_USER" })
	@RequestMapping(value = "/messages/{messageId:[\\p{Digit}]+}/attachments/upload.json", method = RequestMethod.POST)
	@ResponseBody
	public List<Attachment> uploadMessageFiles(@PathVariable Long messageId, MultipartHttpServletRequest request)
			throws NotFoundException, UnAuthorizedException, IOException {

		User currentUser = SecurityHelper.getUser();
		ForumMessage message = forumService.getForumMessage(messageId);

		if (currentUser.isAnonymous() || message.getUser().getUserId() != currentUser.getUserId()) {
			throw new UnAuthorizedException();
		}

		if (messageId < 1) {
			throw new IllegalArgumentException("Message Id can't be " + messageId);
		}

		List<Attachment> list = new ArrayList<Attachment>();
		Iterator<String> names = request.getFileNames();
		while (names.hasNext()) {
			String fileName = names.next();
			MultipartFile mpf = request.getFile(fileName);
			InputStream is = mpf.getInputStream();
			log.debug("upload - file:{}, size:{}, type:{} ", mpf.getOriginalFilename(), mpf.getSize(),
					mpf.getContentType());
			Attachment attachment = attachmentService.createAttachment(7, message.getMessageId(), mpf.getOriginalFilename(), mpf.getContentType(), is, (int) mpf.getSize());
			attachment.setUser(currentUser);
			attachmentService.saveAttachment(attachment);
			list.add(attachment);
		}
		return list;

	}

	@Secured({ "ROLE_USER" })
	@RequestMapping(value = "/messages/attachments/upload.json", method = RequestMethod.POST)
	@ResponseBody
	public List<Attachment> uploadMessageFiles2(
			@RequestParam(value = "messageId", defaultValue = "-1", required = false) Long messageId,
			MultipartHttpServletRequest request) throws NotFoundException, IOException, UnAuthorizedException {

		User user = SecurityHelper.getUser();
		ForumMessage message = forumService.getForumMessage(messageId);
		if (user.isAnonymous() || message.getUser().getUserId() != user.getUserId()) {
			throw new UnAuthorizedException();
		}

		if (messageId < 1) {
			throw new IllegalArgumentException("Message Id can't be " + messageId);
		}

		List<Attachment> list = new ArrayList<Attachment>();
		Iterator<String> names = request.getFileNames();
		while (names.hasNext()) {
			String fileName = names.next();
			MultipartFile mpf = request.getFile(fileName);
			InputStream is = mpf.getInputStream();
			log.debug("upload - file:{}, size:{}, type:{} ", mpf.getOriginalFilename(), mpf.getSize(),
					mpf.getContentType());
			Attachment attachment = attachmentService.createAttachment(7, message.getMessageId(),
					mpf.getOriginalFilename(), mpf.getContentType(), is, (int) mpf.getSize());
			attachmentService.saveAttachment(attachment);
		
			list.add(attachment);
		}
		return list;
	}

	/**
	 * /data/forums/threads/{threadId}/messages/list.json
	 * 
	 * @param boardId
	 * @param threadId
	 * @param request
	 * @return
	 * @throws BoardNotFoundException
	 */
	@RequestMapping(value = "/threads/{threadId:[\\p{Digit}]+}/messages/list.json", method = { RequestMethod.POST,
			RequestMethod.GET })
	@ResponseBody
	public ItemList getMessages(@PathVariable Long threadId,
			@RequestParam(value = "skip", defaultValue = "0", required = false) int skip,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "pageSize", defaultValue = "0", required = false) int pageSize,
			NativeWebRequest request) throws ForumThreadNotFoundException {

		log.debug(" skip: {}, page: {}, pageSize: {}", skip, page, pageSize);

		ForumThread thread = forumService.getForumThread(threadId);

		MessageTreeWalker walker = forumService.getTreeWalker(thread);

		int totalSize = walker.getChildCount(thread.getRootMessage());

		List<ForumMessage> list = getMessages(skip, page, pageSize, walker.getChildIds(thread.getRootMessage()));

		return new ItemList(list, totalSize);
	}

	/**
	 * /data/forums/{threadId}/messages/{messageId}/list.json
	 * 
	 * @param boardId
	 * @param threadId
	 * @param request
	 * @return
	 * @throws ForumMessageNotFoundException
	 * @throws BoardNotFoundException
	 */
	@RequestMapping(value = "/threads/{threadId:[\\p{Digit}]+}/messages/{parentId:[\\p{Digit}]+}/list.json", method = {
			RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ItemList getChildMessages(@PathVariable Long threadId, @PathVariable Long parentId, NativeWebRequest request)
			throws ForumThreadNotFoundException, ForumMessageNotFoundException {

		ForumThread thread = forumService.getForumThread(threadId);
		ForumMessage parent = forumService.getForumMessage(parentId);
		MessageTreeWalker walker = forumService.getTreeWalker(thread);
		int totalSize = walker.getChildCount(parent);
		List<ForumMessage> list = getMessages(walker.getChildIds(parent));
		return new ItemList(list, totalSize);
	}

	/**
	 * /data/forums/threads/{threadId}/messages/{messageId}/comments/add.json?
	 * text=
	 * 
	 * @return
	 * @throws ForumMessageNotFoundException
	 * @throws BoardNotFoundException
	 */
	@RequestMapping(value = "/threads/{threadId:[\\p{Digit}]+}/messages/{messageId:[\\p{Digit}]+}/comments/add_simple.json", method = {
			RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public Result addComment(@PathVariable Long threadId, @PathVariable Long messageId,
			@RequestParam(value = "name", defaultValue = "", required = false) String name,
			@RequestParam(value = "email", defaultValue = "", required = false) String email,
			@RequestParam(value = "text", defaultValue = "", required = true) String text, HttpServletRequest request,
			ModelMap model) {

		Result result = Result.newResult();
		try {
			User user = SecurityHelper.getUser();
			String address = request.getRemoteAddr();

			ForumMessage message = forumService.getForumMessage(messageId);
			Comment newComment = commentService.createComment(Models.FORUM_MESSAGE.getObjectType(),
					message.getMessageId(), user, text);

			newComment.setIPAddress(address);
			if (!StringUtils.isNullOrEmpty(name))
				newComment.setName(name);
			if (!StringUtils.isNullOrEmpty(email))
				newComment.setEmail(email);

			commentService.addComment(newComment);

			result.setCount(1);
		} catch (Exception e) {
			result.setError(e);
		}

		return result;
	}

	@RequestMapping(value = "/threads/{threadId:[\\p{Digit}]+}/messages/{messageId:[\\p{Digit}]+}/comments/add.json", method = {
			RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public Result addMessageComment(@PathVariable Long threadId, @PathVariable Long messageId,
			@RequestBody RequestData reqeustData, HttpServletRequest request, ModelMap model) {
		Result result = Result.newResult();
		try {
			User user = SecurityHelper.getUser();
			String address = request.getRemoteAddr();
			String name = reqeustData.getDataAsString("name", null);
			String email = reqeustData.getDataAsString("email", null);
			String text = reqeustData.getDataAsString("text", null);
			Long parentCommentId = reqeustData.getDataAsLong("parentCommentId", 0L);

			ForumMessage message = forumService.getForumMessage(messageId);
			Comment newComment = commentService.createComment(Models.FORUM_MESSAGE.getObjectType(),
					message.getMessageId(), user, text);

			newComment.setIPAddress(address);
			if (!StringUtils.isNullOrEmpty(name))
				newComment.setName(name);
			if (!StringUtils.isNullOrEmpty(email))
				newComment.setEmail(email);

			if (parentCommentId > 0) {
				Comment parentComment = commentService.getComment(parentCommentId);
				commentService.addComment(parentComment, newComment);
			} else {
				commentService.addComment(newComment);
			}
			result.setCount(1);
		} catch (Exception e) {
			result.setError(e);
		}

		return result;
	}

	@RequestMapping(value = "/threads/{threadId:[\\p{Digit}]+}/messages/{messageId:[\\p{Digit}]+}/comments/list.json", method = {
			RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ItemList getComments(@PathVariable Long threadId, @PathVariable Long messageId, NativeWebRequest request)
			throws ForumMessageNotFoundException {
		ForumMessage message = forumService.getForumMessage(messageId);
		ModelObjectTreeWalker walker = commentService.getCommentTreeWalker(Models.FORUM_MESSAGE.getObjectType(),
				message.getMessageId());
		long parentId = -1L;
		int totalSize = walker.getChildCount(parentId);
		List<Comment> list = walker.children(parentId, new ObjectLoader<Comment>() {
			public Comment load(long commentId) throws NotFoundException {
				return commentService.getComment(commentId);
			}

		});
		return new ItemList(list, totalSize);
	}

	@RequestMapping(value = "/threads/{threadId:[\\p{Digit}]+}/messages/{messageId:[\\p{Digit}]+}/comments/{commentId:[\\p{Digit}]+}/list.json", method = {
			RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ItemList getChildComments(@PathVariable Long threadId, @PathVariable Long messageId,
			@PathVariable Long commentId, NativeWebRequest request) throws ForumMessageNotFoundException {

		ForumMessage message = forumService.getForumMessage(messageId);
		ModelObjectTreeWalker walker = commentService.getCommentTreeWalker(Models.FORUM_MESSAGE.getObjectType(),
				message.getMessageId());

		int totalSize = walker.getChildCount(commentId);
		List<Comment> list = walker.children(commentId, new ObjectLoader<Comment>() {
			public Comment load(long commentId) throws NotFoundException {
				return commentService.getComment(commentId);
			}
		});
		return new ItemList(list, totalSize);
	}

	protected List<ForumMessage> getMessages(int skip, int page, int pageSize, long[] messageIds) {
		if (pageSize == 0 && page == 0) {
			return getMessages(messageIds);
		}

		List<ForumMessage> list = new ArrayList<ForumMessage>();
		for (int i = 0; i < pageSize * page; i++) {
			log.debug("{} : {}", messageIds.length, i);
			if (i >= messageIds.length)
				break;

			if (skip > 0 && i < skip) {
				continue;
			}

			try {
				list.add(forumService.getForumMessage(messageIds[i]));
			} catch (ForumMessageNotFoundException e) {
			}
		}
		return list;
	}

	protected List<ForumMessage> getMessages(long[] messageIds) {
		List<ForumMessage> list = new ArrayList<ForumMessage>(messageIds.length);
		for (long messageId : messageIds) {
			try {
				list.add(forumService.getForumMessage(messageId));
			} catch (ForumMessageNotFoundException e) {
				// ignore..
			}
		}
		return list;
	}
}