import { useState } from "react";
import { login as loginApi } from "../services/authApi";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";

function Login(){

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const {login} = useAuth();

  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();
    
    try {
      const response = await loginApi(email, password);
      console.log("API response", response);
      login(response.data.token);
      console.log("Token passed to AuthContext:", response.data.token);

      navigate("/dashboard");

    } catch (error) {
      console.error(error);
    }
  }
  return(
    <div>
      <h1>Login</h1>
      <form onSubmit={handleSubmit}>
        <input type="email" placeholder="Email" value={email} onChange={(event) => setEmail(event.target.value)}/>
        <input type="password" placeholder="Password" value={password} onChange={(event) => setPassword(event.target.value)}/>
        <button type="submit">Login</button>
      </form>
    </div>
  )
}

export default Login;