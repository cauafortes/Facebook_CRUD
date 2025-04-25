package controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ModelException;
import model.Post;
import model.User;
import model.dao.DAOFactory;
import model.dao.PostDAO;
import model.dao.UserDAO;

@WebServlet(urlPatterns = { "/posts", "/post/save", "/post/update", "/post/delete" })
public class PostsController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String action = req.getRequestURI();

		System.out.println(action);

		switch (action) {
		case "/facebook/posts": {
			// Carregar a lista de posts do banco
			loadPosts(req);
			RequestDispatcher rd = req.getRequestDispatcher("posts.jsp");
			rd.forward(req, resp);
			break;
		}
		case "/facebook/post/save": {
		    String postId = req.getParameter("post_id");
		    if (postId != null && !postId.equals(""))
		        updatePost(req);
		    else
		        insertPost(req);

		    resp.sendRedirect("/facebook/posts");
		    break;
		}
		case "/facebook/post/update": {
			String postId = req.getParameter("postId");

			if (postId != null && !postId.isEmpty()) {
				loadPost(req);
			} else {
				req.setAttribute("post", new Post());
				try {
					List<User> users = ((UserDAO) DAOFactory.createDAO(UserDAO.class)).listAll();
					req.setAttribute("users", users);
				} catch (ModelException e) {
					e.printStackTrace();
				}
			}

			RequestDispatcher rd = req.getRequestDispatcher("/form_post.jsp");
			rd.forward(req, resp);
			break;
		}
		case "/facebook/post/delete": {
			deletePost(req);
			resp.sendRedirect("/facebook/posts");
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + action);
		}
	}

	private void deletePost(HttpServletRequest req) {
		String postIdString = req.getParameter("postId");
		int postId = Integer.parseInt(postIdString);

		Post post = new Post(postId);

		PostDAO dao = DAOFactory.createDAO(PostDAO.class);

		try {
			dao.delete(post);
		} catch (ModelException e) {
			// log no servidor
			e.getCause().printStackTrace();
			e.printStackTrace();
		}
	}

	private void updatePost(HttpServletRequest req) {
		Post post = createPost(req);

		PostDAO dao = DAOFactory.createDAO(PostDAO.class);

		try {
			dao.update(post);
		} catch (ModelException e) {
			// log no servidor
			e.printStackTrace();
		}
	}

	private Post createPost(HttpServletRequest req) {
		String postId = req.getParameter("post_id");
		String postContent = req.getParameter("post_content");
		String userId = req.getParameter("user_id");

		Post post;
		if (postId == null || postId.equals(""))
			post = new Post();
		else
			post = new Post(Integer.parseInt(postId));

		post.setContent(postContent);

		try {
			if (userId != null && !userId.equals("")) {
				UserDAO userDAO = DAOFactory.createDAO(UserDAO.class);
				User user = userDAO.findById(Integer.parseInt(userId));
				post.setUser(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return post;
	}

	private void loadPost(HttpServletRequest req) {
		String postIdParameter = req.getParameter("postId");

		int postId = Integer.parseInt(postIdParameter);

		PostDAO dao = DAOFactory.createDAO(PostDAO.class);

		try {
			Post post = dao.findById(postId);

			if (post == null)
				throw new ModelException("Post não encontrado para alteração");

			req.setAttribute("post", post);
		} catch (ModelException e) {
			// log no servidor
			e.printStackTrace();
		}
	}

	private void insertPost(HttpServletRequest req) {
		Post post = createPost(req);

		PostDAO dao = DAOFactory.createDAO(PostDAO.class);

		try {
			dao.save(post);
		} catch (ModelException e) {
			e.printStackTrace();
		}
	}

	private void loadPosts(HttpServletRequest req) {
		PostDAO dao = DAOFactory.createDAO(PostDAO.class);

		List<Post> posts = null;
		try {
			posts = dao.listAll();
		} catch (ModelException e) {
			// Log no servidor
			e.printStackTrace();
		}

		if (posts != null)
			req.setAttribute("posts", posts);
	}
}
