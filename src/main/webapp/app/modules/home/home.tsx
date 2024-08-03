import React from 'react';
import './home.scss';

const Home: React.FC = () => {
  return (
    <div className="home-container">
      <h1>Welcome to AWS Expert</h1>
      <p>Your gateway to the best cloud services.</p>

      <div className="feature-section">
        <div className="feature">
          <img src="/assets/images/cloud.svg" alt="Cloud Services" />
          <h2>Cloud Expertise</h2>
          <p>Leverage our cloud solutions to boost your business.</p>
        </div>
        <div className="feature">
          <img src="/assets/images/security.svg" alt="Security" />
          <h2>Top Security</h2>
          <p>Secure your data with our state-of-the-art security features.</p>
        </div>
        <div className="feature">
          <img src="/assets/images/support.svg" alt="24/7 Support" />
          <h2>24/7 Support</h2>
          <p>Our team is available around the clock to assist you.</p>
        </div>
      </div>

      <button className="cta-button">Learn More</button>
    </div>
  );
};

export default Home;
