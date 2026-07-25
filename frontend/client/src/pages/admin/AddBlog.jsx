import React, { useEffect, useRef, useState } from 'react'
import { assets, blogCategories } from '../../assets/assets'
import Quill from 'quill';  
import { useAppContext } from '../../context/AppContext';
import toast from 'react-hot-toast';
import {parse} from 'marked';

const AddBlog = () => {

  const { axios } = useAppContext();
  const [isAdding, setIsAdding] = useState(false);
  // when we'll upload blog it'll take some time to upload data to the database using API
  // until then we'll we'll disble button & would show Adding...
  const [loading, setLoading] = useState(false);

  const editorRef = useRef(null);
  const quillRef = useRef(null); // rich text
  
  const [image, setImage] = useState(false);
  const [title, setTitle] = useState('');
  const [subTitle, setSubTitle] = useState('');
  const [category, setCategory] = useState('Startup');
  const [isPublished, setIsPublished] = useState(false);
  
  // in these states we're getting form data
  // we'll send this data on API & to make API call we need Axios

  const onSubmitHandler = async (e) => {
    try {
      e.preventDefault();
      setIsAdding(true) 

      // create blog object
      const blog = {
        title, subTitle, 
        description: quillRef.current.root.innerHTML,
        category, isPublished
      };

      const formData = new FormData();
      formData.append('blog', JSON.stringify(blog)); // stringfy blog data as it is an object
      formData.append('image', image);

      const { data } = await axios.post('/api/blog/add', formData);
      if(data.success){
          toast.success(data.message);
          setImage(false);
          setTitle('');
          quillRef.current.root.innerHTML = '';
          setCategory('Startup'); // default category
      }
      else{
        toast.error(data.message);
      } 
    } catch (error) {
        toast.error(error.message);
    }
  }
  
  useEffect(() => {
    // initiate quill only once
    if(!quillRef.current && editorRef.current){
      quillRef.current = new Quill(editorRef.current, {
        theme: 'snow',
        placeholder: 'Write blog description here...'
      });
    }
  }, [])
  
  const generateContent = async() => {
    if(!title) return toast.error('Please enter a title.');
    try { 
      // it'll take some time to load 
      // use loader 
      setLoading(true);
      const { data } = await axios.post('/api/blog/generate', {prompt: title});
      if(data.success){
        quillRef.current.root.innerHTML = parse(data.content); // parse blog content
      }
      else{
        toast.error(data.message);
      } 
    } catch (error) {
        toast.error(error.message);
    }
    finally{
      setLoading(false); // after that stop loading
    }
  }
  
  return (
    <div className='flex-1 pt-5 px-5 sm:pt-12 sm:pl-16 bg-blue-50/50 min-h-screen'>
      <form onSubmit={onSubmitHandler} className='w-full max-w-3xl'>
        <div className='bg-white w-full p-4 md:p-10 shadow rounded'>
          <p className='text-gray-700 font-medium'>Upload thumbnail</p>
          <label htmlFor="image">
            <img 
              src={!image ? assets.upload_area : URL.createObjectURL(image)} 
              alt="" 
              className='mt-2 h-16 rounded cursor-pointer'
            />
            <input 
              onChange={(e) => setImage(e.target.files[0])} 
              type="file" 
              id='image' 
              hidden 
              required
            />
          </label>
          
          <p className='mt-4 text-gray-700 font-medium'>Blog Title</p>
          <input 
            type="text" 
            placeholder='Type Here' 
            required 
            className='w-full mt-2 p-2 border border-gray-300 outline-none rounded' 
            onChange={e => setTitle(e.target.value)} 
            value={title}
          />
          
          <p className='mt-4 text-gray-700 font-medium'>Sub Title</p>
          <input 
            type="text" 
            placeholder='Type Here' 
            required 
            className='w-full mt-2 p-2 border border-gray-300 outline-none rounded' 
            onChange={e => setSubTitle(e.target.value)} 
            value={subTitle}
          />
          
          <p className='mt-4 text-gray-700 font-medium'>Blog Description</p>
          <div className='w-full mt-2 relative'>
          <div ref={editorRef} className='border border-gray-300 rounded'></div>
            { loading && (
                <div className='absolute inset-0 flex items-center justify-center bg-black/10 rounded'> 
                  <div className='w-10 h-10 rounded-full border-4 border-white/60 border-t-blue-700 animate-spin'></div>
                </div>
            ) }
            <button 
              disabled={loading} //if loading disable button
              onClick={generateContent} 
              type='button' 
              className='absolute bottom-2 right-2 text-xs text-white bg-blue-700 px-4 py-1.5 rounded hover:underline cursor-pointer z-10'
            >
              Generate With AI
            </button>
          </div>

          <p className='mt-4'>Blog Category</p>
          <select onChange={e => setCategory(e.target.value)} name="category" className='mt-2 px-3 py-2 border text-gray-500 border-gray-300
          outline-none rounded'>
            <option value="">Selection Category</option>
            {blogCategories.map((item, index)=>{
              return <option key={index} value={item}>{item}</option>
            })}
          </select>

          <div className='mt-4 flex items-center gap-2'>
            <input 
              type="checkbox" 
              id="published"
              checked={isPublished}
              onChange={e => setIsPublished(e.target.checked)}
              className='cursor-pointer'
            />
            <label htmlFor="published" className='text-gray-700 cursor-pointer'>Publish immediately</label>
          </div>
          
          <button 
            disabled={isAdding}
            type='submit'
            className='mt-6 w-full sm:w-auto px-8 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors'>
            {isAdding ? 'Adding...' : 'Add Blog'}
          </button>
        </div>
      </form>
    </div>
  )
}

export default AddBlog