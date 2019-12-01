package com.romanbrunner.apps.fitnesstracker;


class Exercise
{
    // --------------------
    // Functional code
    // --------------------

    private String name;
    private String token;
    private int repeats;
    private float weight;
    private boolean done;
    private String remarks;

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the token
     */
    public String getToken()
    {
        return token;
    }

    /**
     * @return the repeats
     */
    public int getRepeats()
    {
        return repeats;
    }

    /**
     * @return the weight
     */
    public float getWeight()
    {
        return weight;
    }

    /**
     * @return the done
     */
    public boolean getDone()
    {
        return done;
    }

    /**
     * @return the remarks
     */
    public String getRemarks()
    {
        return remarks;
    }

    Exercise(String name, String token, int repeats, float weight)
    {
        this.name = name;
        this.token = token;
        this.repeats = repeats;
        this.weight = weight;
        done = false;
    }
    Exercise(String name, String token, int repeats, float weight, String remarks)
    {
        this(name, token, repeats, weight);
        this.remarks = remarks;
    }
}