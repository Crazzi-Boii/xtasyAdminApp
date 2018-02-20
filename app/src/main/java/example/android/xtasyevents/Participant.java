/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.android.xtasyevents;

public class Participant {
    private String xtasyid,name,email,college,contact,gender;
    public Participant(String xtasyidInput, String nameInput, String emailInput, String collegeInput, String contactInput, String genderInput) {
        xtasyid=xtasyidInput;
        name=nameInput;
        email=emailInput;
        college=collegeInput;
        contact=contactInput;
        gender=genderInput;
    }
    public String getXtasyid()
    {
        return xtasyid;
    }
    public String getName()
    {
        return name;
    }
    public String getEmail()
    {
        return email;
    }
    public String getCollege()
    {
        return college;
    }
    public String getContact()
    {
        return contact;
    }
    public String getGender()
    {
        return gender;
    }
}

