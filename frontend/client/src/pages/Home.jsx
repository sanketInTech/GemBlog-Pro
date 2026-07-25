import React from 'react'
import Navbar from '../components/Navbar'
import Header from '../components/Header';
import BlogList from '../components/BlogList';
import Footer from '../components/Footer';
import NewsLetter from '../components/Newsletter';

const Home = () => {
  return (
      <>
        <Navbar/>
        <Header/>
        <BlogList/>
        <NewsLetter/>
        <Footer/>
      </>
  );
}

export default Home
