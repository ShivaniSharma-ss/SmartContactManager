package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	@ModelAttribute
	public void addCommonData(Model m,Principal principal)
	{
		String userName=principal.getName();
		//System.out.println(userName);
		//get user using username(email)
		User user=this.userRepository.getUserByUserName(userName);
		//System.out.println("User"+user);
		m.addAttribute("user",user);
		
	}
	
	
	
	
	//dashboard name
@RequestMapping("/index")
	public String dashboard(Model m)
	{
	m.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}

//add form handler
@GetMapping("/add-contact")
public String openAddContactForm(Model model)
{
	model.addAttribute("title","Add Contact");
    model.addAttribute("contact",new Contact());
	return "normal/add_contact_form";	
}

//processing add contact form
@PostMapping("/process-contact")
public String processContact(@ModelAttribute Contact contact,
		@RequestParam("profileImage") MultipartFile file, 
		Principal principal,HttpSession session)
{
	try
	{
	String name=principal.getName();
	User user=this.userRepository.getUserByUserName(name);
	contact.setUser(user);
	user.getContacts().add(contact);
	
	//processing and uploading file
	
	
		if(file.isEmpty())
		{
			//if the file is empty then try our message
			//System.out.println("Image is empty");
			contact.setImage("contact.png");
		}
		else
		{
			//find the file to folder and update the name to contact
			contact.setImage(file.getOriginalFilename());
			//where to upload
			File saveFile=new ClassPathResource("static/images").getFile();
			Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
		//System.out.println("Image is uploaded");
		}
		
      this.userRepository.save(user);
      //System.out.println("Data"+contact);
      //System.out.println("Added to data base");
    //message success......
   session.setAttribute("message",new Message("Your contact is added !! Add more ...","success"));


    }catch(Exception e)
	{
		System.out.println("Error "+e.getMessage());
		//error message
		session.setAttribute("message",new Message("Something went wrong !! Try again ...","danger"));
		
	}
	return "normal/add_contact_form";
}

//show contacts handler
//per page=5[n]
//current page =0[page]
@GetMapping("/show_contacts/{page}")
public String showContacts(@PathVariable("page") Integer page,Model m,Principal p)
{
	m.addAttribute("title","Show User Contacts");
	//send contact list
	String username=p.getName();
	User user=this.userRepository.getUserByUserName(username);
	Pageable pageable=PageRequest.of(page,5);
	int userId=user.getId();
	//System.out.println(userId);
	Page<Contact>contacts=this.contactRepository.findContactsByUser(userId,pageable);
	m.addAttribute("contacts", contacts);
	m.addAttribute("currentPage",page);
	m.addAttribute("totalPages",contacts.getTotalPages());
	
return "normal/show_contacts";	
}

//showing particular contact details
@RequestMapping("/{cId}/contact")
public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal p)
{
	Optional<Contact> contactOptional=this.contactRepository.findById(cId);
	Contact contact=contactOptional.get();
	//
	String userName=p.getName();
User user=	this.userRepository.getUserByUserName(userName);
//System.out.println(user.getId());
//System.out.println(contact.getUser().getId());
if(user.getId()==contact.getUser().getId())
{
model.addAttribute("title",contact.getName());
model.addAttribute("contact",contact);
}	
return "normal/contact_detail";	
}
 
//delete contact handler
@GetMapping("/delete/{cid}")
public String deleteContact(@PathVariable("cid") Integer cid,Principal p,HttpSession session)
{
Contact contact=this.contactRepository.findById(cid).get();

//check id

User user=	this.userRepository.getUserByUserName(p.getName());
user.getContacts().remove(contact);


//contact.setUser(null);
this.userRepository.save(user);
session.setAttribute("message", new Message("Contact deleted Successfully ...","success"));

return "redirect:/user/show_contacts/0";	
}
//open update form handler
@PostMapping("/update-contact/{cid}")
public String updateForm(@PathVariable("cid")Integer cid,Model m)
{
	m.addAttribute("title","Update Contact");
Contact contact=	this.contactRepository.findById(cid).get();
m.addAttribute("contact",contact);
return "normal/update_form";
}

//update contact handler
@RequestMapping(value="/process-update",method=RequestMethod.POST)
public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model model,HttpSession session,Principal p)
{
	try
	{
		//fetch old contact details
		Contact oldContactDetail=this.contactRepository.findById(contact.getcId()).get();
		
		
		
		//image
		if(!file.isEmpty())
		{
			//file work rewrite
			//delete old pic
			File deleteFile=new ClassPathResource("static/images").getFile();
			File file1=new File(deleteFile,oldContactDetail.getImage());
			file1.delete();
			
		//update new pic
			File saveFile=new ClassPathResource("static/images").getFile();
			
			Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			contact.setImage(file.getOriginalFilename());
			
			
		}
		else
		{
			
			contact.setImage(oldContactDetail.getImage());
		}
		User user=this.userRepository.getUserByUserName(p.getName());
		contact.setUser(user);
		this.contactRepository.save(contact);
		session.setAttribute("message",new Message("Your contact is updated... ","success"));
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
return "redirect:/user/"+contact.getcId()+"/contact";	
}
	//your profile handler
@GetMapping("/profile")
public String yourProfile(Model m)
{
	m.addAttribute("title","Profile Page");
	return "normal/profile";
	}
}
