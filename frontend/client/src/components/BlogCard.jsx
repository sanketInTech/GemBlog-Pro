import React from "react";
import { useNavigate } from "react-router-dom";

const BlogCard = ({ blog }) => {
    const { title, description, category, image, id } = blog;
    const navigate = useNavigate();

    const handleCardClick = () => {
        console.log("Navigating to blog:", id);
        navigate(`/blog/${id}`);
    };

    return (
        <div
            onClick={handleCardClick}
            className="w-full rounded-lg overflow-hidden shadow hover:scale-102 hover:shadow-blue-700/25 duration-300 cursor-pointer transform transition-all"
        >
            <img
                src={image}
                alt={title || "Blog image"}
                className="aspect-video object-cover"
            />
            {/* Fixed: Added space between py-1 and inline-block */}
            <span className="ml-5 mt-4 px-3 py-1 inline-block bg-blue-700/20 rounded-full text-blue-700 text-xs">
                {category}
            </span>
            <div className="p-5">
                <h5 className="mb-2 font-medium text-gray-900 line-clamp-2">
                    {title}
                </h5>
                <p
                    className="mb-3 text-xs text-gray-600 line-clamp-3"
                    dangerouslySetInnerHTML={{
                        __html: description?.slice(0, 80) + "...",
                    }}
                />
            </div>
        </div>
    );
};

export default BlogCard;
