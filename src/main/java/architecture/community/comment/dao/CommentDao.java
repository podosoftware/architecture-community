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

package architecture.community.comment.dao;

import architecture.community.comment.Comment;
import architecture.community.comment.CommentTreeWalker;

public interface CommentDao {
	
	public abstract Comment getCommentById(long commentId);
	
	public abstract void createComment(Comment newComment);
	
	public abstract void updateComment(Comment comment);
	
	public abstract CommentTreeWalker getCommentTreeWalker (int objectType, long objectId);

}
