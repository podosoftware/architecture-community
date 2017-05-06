package tests;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.StreamUtils;

import architecture.community.forum.ForumMessage;
import architecture.community.forum.ForumService;
import architecture.community.forum.ForumThread;
import architecture.community.forum.ForumThreadNotFoundException;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration("WebContent/")
@ContextConfiguration(locations = { "classpath:application-community-context.xml" })

public class ForumTest {
	
	private static Logger log = LoggerFactory.getLogger(ForumTest.class);
	
	@Autowired
	private ForumService forumService;

	
	private String getBodyText(String filename){
		
		String body = "";
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			body = StreamUtils.copyToString(cl.getResourceAsStream(filename), Charset.defaultCharset() );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return body;
	}
	
	@Test
	public void testCreateTheadIfNoExist(){
		int objectType = 1;
		long objectId = 1L;
		
		List<ForumThread> list = forumService.getForumThreads(objectType, objectId);
		log.debug(" THREAD COUNT {}" , list.size() );		
		if( list.size() == 0 ){
			ForumMessage rootMessage = forumService.createMessage(objectType, objectId);
			rootMessage.setSubject("인간이란 무엇인가?");
			rootMessage.setBody("인간은 고기 덩어리 인가 아님 다른 존재이가?");
			ForumThread thread = forumService.createThread(objectType, objectId, rootMessage);
			forumService.addThread(objectType, objectId, thread);
		}
		
		for( ForumThread t : list ){
			
			ForumMessage message = t.getRootMessage();
			
			if( message.getMessageId() == 2 && message.getBody().length() < 20000 ){
				message.setBody(getBodyText("text1.txt"));
				forumService.updateMessage(message);				
			}else if( message.getMessageId() == 4 && message.getBody().length() < 20000 ){
				message.setBody(getBodyText("text2.txt"));
				forumService.updateMessage(message);				
			}
			
			log.debug(" FORUM THREAD : " + t );
			log.debug( message.getSubject() );
			log.debug( "UPDATE : {} \n {} ", message.getModifiedDate(), message.getBody() );
		}

		
	}
	
	@Test
	public void testReplayMessage(){
		int objectType = 1;
		long objectId = 1L;
		log.debug("replay ============ ");
		try {
			ForumThread thread = forumService.getForumThread(2);
			log.debug(thread.toString());
			ForumMessage parentMessage = thread.getRootMessage();
			
			ForumMessage replayMessage =forumService.createMessage(objectType, objectId);			
			replayMessage.setSubject("RE:" + parentMessage.getSubject());
			replayMessage.setBody("인간은 고기 덩어리가 맞다...." + new Date());			
			forumService.addMessage(thread, parentMessage, replayMessage);
			
		} catch (ForumThreadNotFoundException e) {
			e.printStackTrace();
		}
		
		
		
		
		
	}
}
