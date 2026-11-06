import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { api, getToken, setToken } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      if (!getToken()) {
        setReady(true);
        return;
      }
      try {
        const me = await api('/api/auth/me');
        if (!cancelled) {
          setUser(me);
        }
      } catch {
        setToken(null);
        if (!cancelled) {
          setUser(null);
        }
      } finally {
        if (!cancelled) {
          setReady(true);
        }
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, []);

  const value = useMemo(() => ({
    user,
    ready,
    async login(email, password) {
      const result = await api('/api/auth/login', {
        method: 'POST',
        body: { email, password },
        auth: false,
      });
      setToken(result.token);
      setUser(result.user);
      return result.user;
    },
    async register(payload) {
      const result = await api('/api/auth/register', {
        method: 'POST',
        body: payload,
        auth: false,
      });
      setToken(result.token);
      setUser(result.user);
      return result.user;
    },
    logout() {
      setToken(null);
      setUser(null);
    },
    updateUser(next) {
      setUser(next);
    },
  }), [user, ready]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
