export const environment = {
	production: true,
	// In production environment frontend part is expected to be bundled with the backend 
	// and served from same server as the backend API, so we can use a relative path here.
	apiURL: '/api',
};
