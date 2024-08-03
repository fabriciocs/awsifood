# Define the path for the home.scss file
$homeScssPath = "C:\repos\awsexpert\src\main\webapp\app\modules\home\home.scss"

# Ensure the directory structure exists
$directoryPath = Split-Path -Path $homeScssPath -Parent
if (-Not (Test-Path -Path $directoryPath)) {
    New-Item -ItemType Directory -Path $directoryPath -Force
}

# Define the content for the home.scss file
$scssContent = @"
.home-container {
  text-align: center;
  padding: 20px;
  background-color: #f8f8f8;

  h1 {
    font-size: 2.5rem;
    color: #333;
  }

  p {
    font-size: 1.2rem;
    color: #666;
  }

  .feature-section {
    display: flex;
    justify-content: space-around;
    margin-top: 40px;

    .feature {
      background-color: #fff;
      border-radius: 8px;
      padding: 20px;
      width: 30%;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
      transition: transform 0.3s ease;

      &:hover {
        transform: translateY(-10px);
      }

      img {
        width: 100px;
        height: auto;
        margin-bottom: 15px;
      }

      h2 {
        font-size: 1.5rem;
        color: #333;
        margin: 15px 0;
      }

      p {
        font-size: 1rem;
        color: #777;
      }
    }
  }

  .cta-button {
    margin-top: 40px;
    padding: 10px 20px;
    font-size: 1.2rem;
    color: #fff;
    background-color: #ff5722;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    transition: background-color 0.3s ease;

    &:hover {
      background-color: #e64a19;
    }
  }
}
"@

# Write the content to the home.scss file
Set-Content -Path $homeScssPath -Value $scssContent

# Add changes to git, commit, and push
cd C:\repos\awsexpert
git add $homeScssPath
git commit -m "Add home.scss for styling the Home component"
git push origin feat/view
