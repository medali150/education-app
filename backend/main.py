import mysql.connector
from fastapi import FastAPI, HTTPException, Query
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware
from typing import Optional, List
from passlib.context import CryptContext

# Password hashing
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

database = mysql.connector.connect(
    host="localhost",
    user="root",
    password="",
    database="student_db",
    connection_timeout=60
)
cursor = database.cursor(dictionary=True)

# Create admins table if it doesn't exist
cursor.execute("""
    CREATE TABLE IF NOT EXISTS admins (
        admin_id INT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        email VARCHAR(255) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
""")
database.commit()

# Create professors table if it doesn't exist
cursor.execute("""
    CREATE TABLE IF NOT EXISTS professors (
        professor_id INT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        email VARCHAR(255) NOT NULL UNIQUE,
        department VARCHAR(255),
        password VARCHAR(255) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
""")
database.commit()

# Drop and recreate courses table
cursor.execute("DROP TABLE IF EXISTS courses")
database.commit()

# Create courses table
cursor.execute("""
    CREATE TABLE IF NOT EXISTS courses (
        course_id INT AUTO_INCREMENT PRIMARY KEY,
        course_name VARCHAR(255) NOT NULL,
        course_field VARCHAR(255) NOT NULL,
        course_duration VARCHAR(255) NOT NULL,
        course_price VARCHAR(255) NOT NULL,
        professor_id INT NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (professor_id) REFERENCES professors(professor_id)
    )
""")
database.commit()

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class Student(BaseModel):
    name: str
    age: int
    email: str
    department: str
    password: str

class StudentUpdate(BaseModel):
    name: Optional[str]
    age: Optional[int]
    email: Optional[str]
    department: Optional[str]

class Course(BaseModel):
    course_name: str
    course_field: str
    course_duration: str
    course_price: str

class StudentLogin(BaseModel):
    name: str
    email: str
    password: str

class AdminModel(BaseModel):
    name: str
    email: str
    password: str

class LoginModel(BaseModel):
    email: str
    password: str

class ProfessorModel(BaseModel):
    name: str
    email: str
    department: Optional[str]
    password: str

class ProfessorUpdate(BaseModel):
    name: Optional[str]
    email: Optional[str]
    department: Optional[str]
    password: Optional[str]

class ProfessorLogin(BaseModel):
    email: str
    password: str

class AdminLogin(BaseModel):
    email: str
    password: str

@app.get("/")
def root():
    return {
        "message": "Welcome to the Student Management API",
        "endpoints": {
            "test_db": "/test-db",
            "add_student": "/addstudent",
            "login_student": "/loginstudent",
            "student_profile": "/profile",
            "all_students": "/students",
            "add_course": "/addcourse",
            "all_courses": "/courses",
            "admin_login": "/admin/login",
            "admin_register": "/admin/register",
            "admin_students": "/admin/students",
            "admin_professors": "/admin/professors"
        }
    }

@app.get("/test-db")
def test_db_connection():
    try:
        cursor.execute("SELECT 1")
        cursor.fetchone()
        return {"message": "Database connection is working"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/addstudent")
def add_student(student: Student):
    try:
        sql = "INSERT INTO STUDENTS (name, age, email, department) VALUES (%s, %s, %s, %s)"
        values = (student.name, student.age, student.email, student.department)
        cursor.execute(sql, values)
        database.commit()
        return {"message": "Student added successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/loginstudent")
def login_student(student: StudentLogin):
    try:
        sql = "SELECT students_id, name, email FROM STUDENTS WHERE name = %s AND email = %s"
        values = (student.name, student.email)
        cursor.execute(sql, values)
        result = cursor.fetchone()

        if result:
            return {
                "message": "Login successful",
                "student_id": result["students_id"],
                "name": result["name"],
                "role": "student"
            }
        else:
            raise HTTPException(status_code=401, detail="Invalid credentials")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/admin/register")
def register_admin(admin: AdminModel):
    try:
        print(f"Attempting to register admin with email: {admin.email}")  # Debug log
        # Check if admin already exists
        cursor.execute("SELECT * FROM admins WHERE email = %s", (admin.email,))
        existing_admin = cursor.fetchone()
        print(f"Existing admin check: {existing_admin}")  # Debug log

        if existing_admin:
            print("Admin already exists")  # Debug log
            raise HTTPException(status_code=400, detail="Admin already exists")

        # Hash password
        hashed_password = pwd_context.hash(admin.password)
        print("Password hashed successfully")  # Debug log

        # Insert new admin
        sql = "INSERT INTO admins (name, email, password) VALUES (%s, %s, %s)"
        values = (admin.name, admin.email, hashed_password)
        cursor.execute(sql, values)
        database.commit()
        print("Admin inserted successfully")  # Debug log

        # Get the newly created admin
        cursor.execute("SELECT * FROM admins WHERE email = %s", (admin.email,))
        new_admin = cursor.fetchone()
        print(f"New admin created: {new_admin}")  # Debug log

        return {
            "message": "Admin registered successfully",
            "admin_id": new_admin["admin_id"],
            "name": new_admin["name"],
            "email": new_admin["email"]
        }
    except HTTPException as he:
        raise he
    except Exception as e:
        print(f"Error in admin registration: {str(e)}")  # Debug log
        raise HTTPException(status_code=500, detail="Internal server error")

@app.post("/admin/login")
def login_admin(login: AdminLogin):
    try:
        email = login.email.strip()
        print(f"Attempting login for admin with email: {email}")  # Debug log
        cursor.execute("SELECT * FROM admins WHERE email = %s", (email,))
        admin = cursor.fetchone()
        print(f"Found admin: {admin}")  # Debug log

        if not admin:
            print("Admin not found")  # Debug log
            raise HTTPException(status_code=401, detail="Invalid credentials")

        # Verify password
        print(f"Plain password: '{login.password}'")
        print(f"Hashed password: '{admin['password']}'")
        print("Password verification result:", pwd_context.verify(login.password, admin["password"]))

        if not pwd_context.verify(login.password, admin["password"]):
            print("Password verification failed")  # Debug log
            raise HTTPException(status_code=401, detail="Invalid credentials")

        print("Login successful")  # Debug log
        return {
            "message": "Login successful",
            "admin_id": admin["admin_id"],
            "name": admin["name"],
            "email": admin["email"],
            "token": "dummy-token",
            "role": "admin"
        }
    except HTTPException as he:
        raise he
    except Exception as e:
        print(f"Error in admin login: {str(e)}")  # Debug log
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/admin/students")
def get_all_students():
    try:
        cursor.execute("SELECT * FROM STUDENTS")
        results = cursor.fetchall()
        return {"students": results}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/admin/students")
def create_student(student: Student):
    try:
        cursor.execute("SELECT * FROM STUDENTS WHERE email = %s", (student.email,))
        if cursor.fetchone():
            raise HTTPException(status_code=400, detail="Student already exists")

        sql = "INSERT INTO STUDENTS (name, age, email, department) VALUES (%s, %s, %s, %s)"
        values = (student.name, student.age, student.email, student.department)
        cursor.execute(sql, values)
        database.commit()
        return {"message": "Student created successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.put("/admin/students/{student_id}")
def update_student(student_id: int, student: StudentUpdate):
    try:
        cursor.execute("SELECT * FROM STUDENTS WHERE students_id = %s", (student_id,))
        if not cursor.fetchone():
            raise HTTPException(status_code=404, detail="Student not found")

        update_fields = []
        values = []
        if student.name:
            update_fields.append("name = %s")
            values.append(student.name)
        if student.age:
            update_fields.append("age = %s")
            values.append(student.age)
        if student.email:
            update_fields.append("email = %s")
            values.append(student.email)
        if student.department:
            update_fields.append("department = %s")
            values.append(student.department)

        if not update_fields:
            raise HTTPException(status_code=400, detail="No fields to update")

        values.append(student_id)
        sql = f"UPDATE STUDENTS SET {', '.join(update_fields)} WHERE students_id = %s"
        cursor.execute(sql, values)
        database.commit()

        return {"message": "Student updated successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.delete("/admin/students/{student_id}")
def delete_student(student_id: int):
    try:
        cursor.execute("SELECT * FROM STUDENTS WHERE students_id = %s", (student_id,))
        if not cursor.fetchone():
            raise HTTPException(status_code=404, detail="Student not found")

        cursor.execute("DELETE FROM STUDENTS WHERE students_id = %s", (student_id,))
        database.commit()
        return {"message": "Student deleted successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/admin/professors")
def get_all_professors():
    try:
        cursor.execute("SELECT * FROM professors")
        results = cursor.fetchall()
        return {"professors": results}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/admin/professors")
def create_professor(professor: ProfessorModel):
    try:
        try:
            cursor.execute("SELECT * FROM professors WHERE email = %s", (professor.email,))
            if cursor.fetchone():
                raise HTTPException(status_code=400, detail="Professor already exists")

            hashed_password = pwd_context.hash(professor.password)
            sql = "INSERT INTO professors (name, email, department, password) VALUES (%s, %s, %s, %s)"
            values = (professor.name, professor.email, professor.department, hashed_password)
            cursor.execute(sql, values)
            database.commit()
            
            # Get the newly created professor
            cursor.execute("SELECT * FROM professors WHERE email = %s", (professor.email,))
            new_professor = cursor.fetchone()
            
            # Return the new professor data
            return new_professor
        except Exception as e:
            raise HTTPException(status_code=500, detail=str(e))
    except Exception as e:
        # Handle outer exceptions if needed
        raise HTTPException(status_code=500, detail=f"Outer exception: {str(e)}")

@app.put("/admin/professors/{professor_id}")
def update_professor(professor_id: int, professor: ProfessorUpdate):
    try:
        cursor.execute("SELECT * FROM professors WHERE professor_id = %s", (professor_id,))
        if not cursor.fetchone():
            raise HTTPException(status_code=404, detail="Professor not found")

        update_fields = []
        values = []
        if professor.name:
            update_fields.append("name = %s")
            values.append(professor.name)
        if professor.email:
            update_fields.append("email = %s")
            values.append(professor.email)
        if professor.department:
            update_fields.append("department = %s")
            values.append(professor.department)
        if professor.password:
            update_fields.append("password = %s")
            values.append(pwd_context.hash(professor.password))

        if not update_fields:
            raise HTTPException(status_code=400, detail="No fields to update")

        values.append(professor_id)
        sql = f"UPDATE professors SET {', '.join(update_fields)} WHERE professor_id = %s"
        cursor.execute(sql, values)
        database.commit()
        
        # Get the updated professor data
        cursor.execute("SELECT * FROM professors WHERE professor_id = %s", (professor_id,))
        updated_professor = cursor.fetchone()
        
        # Return the updated professor data instead of just a message
        return updated_professor
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.delete("/admin/professors/{professor_id}")
def delete_professor(professor_id: int):
    try:
        cursor.execute("SELECT * FROM professors WHERE professor_id = %s", (professor_id,))
        if not cursor.fetchone():
            raise HTTPException(status_code=404, detail="Professor not found")

        cursor.execute("DELETE FROM professors WHERE professor_id = %s", (professor_id,))
        database.commit()
        return {"message": "Professor deleted successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/addcourse")
def add_course(course: Course, professor_id: int = Query(..., description="The ID of the professor creating the course")):
    try:
        # Verify professor exists
        cursor.execute("SELECT * FROM professors WHERE professor_id = %s", (professor_id,))
        professor = cursor.fetchone()
        if not professor:
            raise HTTPException(status_code=404, detail="Professor not found")

        # Insert new course
        sql = """
            INSERT INTO courses 
            (course_name, course_field, course_duration, course_price, professor_id) 
            VALUES (%s, %s, %s, %s, %s)
        """
        values = (
            course.course_name, 
            course.course_field, 
            course.course_duration, 
            course.course_price,
            professor_id
        )
        cursor.execute(sql, values)
        database.commit()
        
        # Get the newly created course
        cursor.execute("SELECT * FROM courses WHERE course_id = LAST_INSERT_ID()")
        new_course = cursor.fetchone()
        
        return {
            "message": "Course added successfully",
            "course": new_course
        }
    except Exception as e:
        print(f"Error in course creation: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/courses")
def show_courses():
    try:
        cursor.execute("""
            SELECT c.*, p.name as professor_name, p.email as professor_email 
            FROM courses c
            INNER JOIN professors p ON c.professor_id = p.professor_id
        """)
        results = cursor.fetchall()
        return {"courses": results}
    except Exception as e:
        print(f"Error fetching courses: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/profile")
def get_student_profile(student_id: int = Query(..., description="The ID of the student")):
    try:
        sql = "SELECT * FROM STUDENTS WHERE students_id = %s"
        values = (student_id,)
        cursor.execute(sql, values)
        result = cursor.fetchone()

        if result:
            return {
                "student_id": result["students_id"],
                "name": result["name"],
                "age": result["age"],
                "email": result["email"],
                "department": result["department"]
            }
        else:
            raise HTTPException(status_code=404, detail="Student not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/auth/professor/login")
def login_professor(professor: ProfessorLogin):
    try:
        print(f"Attempting login for professor with email: {professor.email}")  # Debug log
        cursor.execute("SELECT professor_id, email, password FROM professors WHERE email = %s", (professor.email,))
        result = cursor.fetchone()
        print(f"Query result: {result}")  # Debug log

        if not result:
            print("Professor not found")  # Debug log
            raise HTTPException(status_code=401, detail="Invalid credentials")

        # Verify password
        print(f"Verifying password for professor: {result['email']}")  # Debug log
        if not pwd_context.verify(professor.password, result["password"]):
            print("Password verification failed")  # Debug log
            raise HTTPException(status_code=401, detail="Invalid credentials")

        print("Login successful")  # Debug log
        return {
            "message": "Login successful",
            "professor_id": result["professor_id"],
            "email": result["email"],
            "role": "professor"
        }
    except HTTPException as he:
        raise he
    except Exception as e:
        print(f"Error in professor login: {str(e)}")  # Debug log
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/admin/students/{student_id}")
def get_student(student_id: int):
    try:
        cursor.execute("SELECT * FROM STUDENTS WHERE students_id = %s", (student_id,))
        result = cursor.fetchone()
        if not result:
            raise HTTPException(status_code=404, detail="Student not found")
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/admin/professors/{professor_id}")
def get_professor(professor_id: int):
    try:
        cursor.execute("SELECT * FROM professors WHERE professor_id = %s", (professor_id,))
        professor = cursor.fetchone()
        if not professor:
            raise HTTPException(status_code=404, detail="Professor not found")
        return professor
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    