import React from "react";

export default function Contact() {
  return (
    <div className="container">
      <h1>Contact Us</h1>
      <p className="lead">
        Get in touch with the FU News Management team.
      </p>
      <div className="row mt-4">
        <div className="col-md-6">
          <h5>Contact Information</h5>
          <ul className="list-unstyled">
            <li className="mb-2">
              <strong>Email:</strong> contact@funews.edu.vn
            </li>
            <li className="mb-2">
              <strong>Phone:</strong> (028) 7300 1866
            </li>
            <li className="mb-2">
              <strong>Address:</strong> FPT University, Ha Noi City, Vietnam
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
}
