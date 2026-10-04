import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route } from "react-router";
import Layout from "./components/Layout.tsx"
import Home from "./pages/Home.tsx"
import Play from "./pages/Play.tsx"
import Acknowledgements from './pages/Acknowledgements.tsx';
import Privacy from './pages/Privacy.tsx';
import News from './pages/News.tsx';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<Home />} />
          <Route path="news" element={<News />} />
          <Route path="play" element={<Play />} />
          <Route path="acknowledgements" element={<Acknowledgements />} />
          <Route path="privacy" element={<Privacy />} />  // basically just add another of these for each page, changing the element and path
        </Route>
      </Routes>
    </BrowserRouter>
  </StrictMode>,
)
