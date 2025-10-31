import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor added to include auth credentials 
api.interceptors.request.use(
  (config) => {
    const authHeader = sessionStorage.getItem('authHeader');
    if(authHeader) {
      config.headers.Authorization = authHeader;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export const fetchTrades = () => api.get('/trades');

export const fetchAllUsers = async () => {
  console.log("Fetching all users from the API");
  return await api.get('/users').then((res) => {return res});
};

export const createUser = (user) => api.post('/users', user);

export const fetchUserProfiles = () => api.get('/userProfiles');

export const updateUser = (id, user) => api.put(`/users/${id}`, user);

export const authenticate = (user: string, pass: string) => {
  const authHeader = 'Basic ' + btoa(`${user}:${pass}`);
  return api.post(`/login/${user}`, null, {
    headers: {
      Authorization: pass
    }
  }).then(response => {
    // Store the auth header for subsequent requests
    sessionStorage.setItem('authHeader', authHeader);
    return response;
  });
}

export const logout = () => {
  sessionStorage.removeItem('authHeader');
}

export const getUserByLogin = (login: string) => {
    return api.get(`/users/loginId/${login}`,{
    headers: {
      Authorization: sessionStorage.getItem('authHeader')
    }
  })
}
export default api;

