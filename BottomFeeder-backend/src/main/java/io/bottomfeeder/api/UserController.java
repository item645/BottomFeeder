package io.bottomfeeder.api;

import static io.bottomfeeder.config.Constants.API_URL_USERS;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.bottomfeeder.api.model.Response;
import io.bottomfeeder.api.model.UserRequest;
import io.bottomfeeder.api.model.UserResponse;
import io.bottomfeeder.data.DataExportService;
import io.bottomfeeder.data.DataImportService;
import io.bottomfeeder.security.Role;
import io.bottomfeeder.user.UserService;

/**
 * REST controller for managing application users.
 * Accessible to admins only.
 */
@RestController
@RequestMapping(API_URL_USERS)
@Secured(Role.Name.ADMIN)
class UserController {

	private final UserService userService;
	private final DataImportService dataImportService;
	private final DataExportService dataExportService;

	
	public UserController(UserService userService,
			DataImportService dataImportService, 
			DataExportService dataExportService) {
		this.userService = userService;
		this.dataImportService = dataImportService;
		this.dataExportService = dataExportService;
	}


	@GetMapping
	public Response<List<UserResponse>> getAllUsers() {
		return new Response<>(userService.getAllUsers().stream().map(UserResponse::new).collect(Collectors.toList()));
	}
	
	
	@GetMapping("/{id}")
	public Response<UserResponse> getUser(@PathVariable long id) {
		return new Response<>(new UserResponse(userService.getUser(id)));
	}
	
	
	@PostMapping
	public Response<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
		var user = userService.createUser(userRequest.login(), userRequest.password(), userRequest.role());
		var message = String.format("User '%s' created successfully", user.getLogin());
		return new Response<>(message, new UserResponse(user));
	}
	
	
	@PutMapping
	public Response<UserResponse> updateUser(@Valid @RequestBody UserRequest userRequest) {
		var updatedUser = userService.updateUser(userRequest.id(), userRequest.login(), userRequest.role());
		var message = String.format("User '%s' updated successfully", updatedUser.getLogin());
		return new Response<>(message, new UserResponse(updatedUser));
	}
	
	
	@DeleteMapping("/{id}")
	public Response<Void> deleteUser(@PathVariable long id) {
		userService.deleteUser(id);
		return new Response<>("User deleted successfully");
	}
	
	
	@PostMapping("/import")
	public Response<Void> importUsers(InputStream usersData) throws IOException {
		dataImportService.importUsers(usersData.readAllBytes());
		return new Response<>("Users imported successfully");
	}
	
	
	@GetMapping("/export")
	public ResponseEntity<byte[]> exportUsers() {
		return Utils.createBinaryJsonResponse(dataExportService.exportUsersData());
	}
	
}
